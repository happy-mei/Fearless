package codegen.js;

import codegen.MIR;
import id.Id;
import main.js.LogicMainJs;
import utils.IoErr;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record JsProgram(List<JsFile> files) {
  public JsProgram(LogicMainJs main, MIR.Program program) {
    this(new ToJsProgram(main, program).of());
  }

  public void writeJsFiles(Path output) {
    IoErr.of(() -> _writeJsFiles(output));
  }

  private void _writeJsFiles(Path output) throws IOException {
    for (var file : files) {
      Path fullPath = output.resolve(file.path());
      if (file.path().endsWith(".wasm")) {
        // Special handling for WASM files
        Files.createDirectories(fullPath.getParent());
        Files.write(fullPath, file.code().getBytes(StandardCharsets.ISO_8859_1),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
      } else {
        // Normal JS files
        Files.createDirectories(fullPath.getParent());
        Files.write(fullPath, file.code().getBytes(),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
      }
    }
  }
}

record ToJsProgram(LogicMainJs main, MIR.Program program) {
  public List<JsFile> of(){
    ArrayList<JsFile> jsFiles = generateFiles();
    List<JsFile> magicFiles = main.io().magicJsFiles();
    assert !magicFiles.isEmpty() : "Failed to read magic files";
    jsFiles.addAll(magicFiles);
    return Collections.unmodifiableList(jsFiles);
  }

  public ArrayList<JsFile> generateFiles() {
    var jsFiles = new ArrayList<JsFile>();
    var gen = new JsCodegen(program);
    // Store base type exports
    Map<String, List<String>> baseExports = new HashMap<>();
    // define a tiny record for staged files
    record Staged(Id.DecId typeId, StringBuilder content) {}
    // Map from output file path -> staged info
    Map<Path, Staged> staged = new LinkedHashMap<>();

    // --- First pass: generate all interfaces and stage them (do NOT write files yet) ---
    for (MIR.Package pkg : program.pkgs()) {
      String pkgPath = pkg.name().replace(".", "/") + "/";
      for (MIR.TypeDef def : pkg.defs().values()) {
        Id.DecId typeId = def.name();
        var funsList = pkg.funs().stream()
          .filter(f -> f.name().d().equals(typeId))
          .toList();
        // Generate interface-like content (abstract base & static helpers)
        String typeDefContent = gen.visitTypeDef(def, funsList);
        if (typeDefContent.isEmpty()) continue;

        String fileName = gen.id.getSimpleName(typeId) + ".js";
        Path filePath = Path.of(pkgPath).resolve(fileName);

        // Stash content in a mutable StringBuilder so we can append impl later
        StringBuilder sb = new StringBuilder();
        sb.append(typeDefContent);
        staged.put(filePath, new Staged(typeId, sb));

        // Track base.* for merged index
        if (pkg.name().startsWith("base")) {
          baseExports.computeIfAbsent(pkg.name(), k -> new ArrayList<>())
            .add(gen.id.getSimpleName(def.name()));
        }
      }
    }

    // --- Second step: attach Impl classes into staged files ---
    for (var e : gen.freshImpls.entrySet()) {
      Id.DecId typeId = e.getKey();
      String implContent = e.getValue();
      Path filePath = Path.of(typeId.pkg().replace(".", "/"))
        .resolve(gen.id.getSimpleName(typeId) + ".js");
      StringBuilder sb;
      if (staged.containsKey(filePath)) {
        sb = staged.get(filePath).content();
      } else {
        sb = new StringBuilder();
        staged.put(filePath, new Staged(typeId, sb));
      }
      // Append Impl class
      sb.append("\n\n").append(implContent);
      // If there’s a queued singleton assignment, append it now
      String className = gen.id.getFullName(typeId);
      if (gen.freshSingletons.containsKey(className)) {
        sb.append("\n").append(gen.freshSingletons.get(className));
      }
    }

    // --- Now compute imports per file and finalize JsFiles ---
    for (var entry : staged.entrySet()) {
      Path filePath = entry.getKey();
      Staged stagedInfo = entry.getValue();
      Id.DecId typeId = stagedInfo.typeId();
      String combinedContent = stagedInfo.content().toString();

      // Compute imports based on combined content (so importLines reflect both interface and impl)
      String importLines = computeImports(typeId.pkg(), gen.id.getFullName(typeId), combinedContent);

      jsFiles.add(new JsFile(filePath, importLines + combinedContent));
    }

    // Now generate merged base/index.js (unchanged)
    if (!baseExports.isEmpty()) {
      JsFile baseIndexJs = createBaseIndexJs(baseExports);
      jsFiles.add(baseIndexJs);
    }
    return jsFiles;
  }

  private String computeImports(String pkg, String className, String content) {
    Set<String> baseImports = new TreeSet<>();
    Set<String> otherImports = new TreeSet<>();
    boolean needsEnsureWasm = false;

    // Pattern matches full-encoded names:
    //   <pkg>$$<pkg>$$...$$<TypeOr_underscoreStart>_<digits>  (optionally followed by Impl)
    // Examples matched: base$$iter$$Sum_0, base$$iter$$Sum_0Impl, base$$_InfoToJson_0, test$$Test_0
    Pattern p = Pattern.compile(
      "([a-z][a-z0-9_]*(?:\\$\\$[a-z][a-z0-9_]*)*\\$\\$[A-Za-z_][A-Za-z0-9_$]*_\\d+)(Impl)?"
    );
    Matcher m = p.matcher(content);
    while (m.find()) {
      String base = m.group(1);
      String impl = m.group(2);
      String dep = (impl != null) ? base + "Impl" : base;
      if (dep.equals(className) || dep.equals(className + "Impl")) continue;

      if (dep.startsWith("base$$")) {
        baseImports.add(dep);
      } else {
        String importPath = getRelativeImportPath(pkg, base);
        otherImports.add("import { " + dep + " } from \"" + importPath + "\";");
      }
    }

    // Special-case: runtime helpers (rt$$Numbers, rt$$Other...)
    Pattern rt = Pattern.compile("\\brt\\$\\$([A-Za-z0-9_]+)\\.");
    Matcher rm = rt.matcher(content);
    while (rm.find()) {
      String dep = "rt$$" + rm.group(1);
      String importPath = getRelativeImportPath(pkg, "rt-js/" + rm.group(1));
      otherImports.add("import { " + dep + " } from \"" + importPath + "\";");

      if (dep.equals("rt$$NativeRuntime")) {
        needsEnsureWasm = true;
      }
    }

    // Merge base$$ imports into one line
    StringBuilder importBlock = new StringBuilder();

    // Merge all base$$ imports into one line pointing to top-level base/index.js
    if (!baseImports.isEmpty()) {
      StringBuilder prefix = new StringBuilder();
      if (!pkg.isEmpty()) {
        int depth = pkg.split("\\.").length;
        for (int i = 0; i < depth; i++) prefix.append("../");
      } else {
        prefix.append("./");
      }
      importBlock.append("import { ")
        .append(String.join(", ", baseImports))
        .append(" } from \"")
        .append(prefix)
        .append("base/index.js\";\n");
    }

    for (String imp : otherImports) {
      importBlock.append(imp).append("\n");
    }

    if (importBlock.length() > 0) {
      importBlock.append("\n");
    }
    if (needsEnsureWasm) {
      importBlock.append("(async function(){ await rt$$NativeRuntime.ensureWasm(); })();\n\n");
    }

    return importBlock.toString();
  }

  private String getRelativeImportPath(String pkg, String encodedDep) {
    String[] parts = encodedDep.split("\\$\\$");
    String depPkg = String.join("/", Arrays.copyOf(parts, parts.length - 1));
    String depFile = parts[parts.length - 1];
    String depPath = depPkg.isEmpty() ? depFile : depPkg + "/" + depFile;

    // compute relative path from currentPkg
    String currentPath = pkg.isEmpty() ? "" : pkg.replace(".", "/");
    int depth = currentPath.isEmpty() ? 0 : currentPath.split("/").length;

    StringBuilder prefix = new StringBuilder();
    if (depth == 0) {
      prefix.append("./");
    } else {
      for (int i = 0; i < depth; i++) {
        prefix.append("../");
      }
    }
    return prefix + depPath + ".js";
  }

  // Create base/index.js to make import lines clearer
  private JsFile createBaseIndexJs(Map<String, List<String>> baseExports) {
    StringBuilder rootIndex = new StringBuilder();
    for (var entry : baseExports.entrySet()) {
      String pkg = entry.getKey(); // e.g., "base", "base.json"
      List<String> typeNames = entry.getValue();
      for (String typeName : typeNames) {
        // Compute flattened export name: base$$json$$Type_0
        String flattenedName = pkg.replace(".", "$$") + "$$" + typeName;
        String simpleFile = typeName; // the actual file name
        String relPath;
        if (pkg.equals("base")) {
          relPath = "./" + simpleFile + ".js";
        } else {
          String pkgPath = pkg.replace(".", "/");
          relPath = "./" + pkgPath.substring(5) + "/" + simpleFile + ".js"; // remove "base/" prefix
        }
        rootIndex.append("export { ").append(flattenedName)
          .append(" } from '").append(relPath).append("';\n");
      }
    }
    return new JsFile(Path.of("base/index.js"), rootIndex.toString());
  }

}