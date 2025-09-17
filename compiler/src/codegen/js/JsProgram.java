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

    // Store exports grouped by base subfolder
    Map<String, Map<Id.DecId, String>> baseExportsByFolder = new HashMap<>();

    // tiny record for staged files
    record Staged(Id.DecId typeId, StringBuilder content) {}
    Map<Path, Staged> staged = new LinkedHashMap<>();

    // --- First pass: generate interface defs ---
    for (MIR.Package pkg : program.pkgs()) {
      String pkgPath = pkg.name().replace(".", "/") + "/";
      for (MIR.TypeDef def : pkg.defs().values()) {
        Id.DecId typeId = def.name();
        var funsList = pkg.funs().stream()
          .filter(f -> f.name().d().equals(typeId))
          .toList();

        String typeDefContent = gen.visitTypeDef(def, funsList);
        if (typeDefContent.isEmpty()) continue;

        String fileName = gen.id.getSimpleName(typeId) + ".js";
        Path filePath = Path.of(pkgPath).resolve(fileName);

        StringBuilder sb = new StringBuilder();
        sb.append(typeDefContent);
        staged.put(filePath, new Staged(typeId, sb));

        // Track base.* types for per-folder index.js
        if (pkg.name().startsWith("base")) {
          String folder = pkg.name().replace(".", "/"); // e.g. base, base/flows, base/json
          baseExportsByFolder
            .computeIfAbsent(folder, k -> new LinkedHashMap<>())
            .put(typeId, gen.id.getSimpleName(typeId));
        }
      }
    }

    // --- Second pass: attach Impl classes ---
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
      sb.append("\n\n").append(implContent);
      // If there’s a queued singleton assignment, append it now
      String className = gen.id.getFullName(typeId);
      if (gen.freshSingletons.containsKey(className)) {
        sb.append("\n").append(gen.freshSingletons.get(className));
      }
    }

    // --- Finalize files with imports ---
    for (var entry : staged.entrySet()) {
      Path filePath = entry.getKey();
      Staged stagedInfo = entry.getValue();
      Id.DecId typeId = stagedInfo.typeId();
      String combinedContent = stagedInfo.content().toString();

      String importLines = computeImports(typeId.pkg(), gen.id.getFullName(typeId), combinedContent);

      jsFiles.add(new JsFile(filePath, importLines + combinedContent));
    }

    // --- Generate index.js for each base/* folder ---
    for (var entry : baseExportsByFolder.entrySet()) {
      String folder = entry.getKey(); // e.g. "base", "base/flows"
      Map<Id.DecId, String> exports = entry.getValue();
      JsFile idx = createIndexJsForFolder(folder, exports, gen);
      jsFiles.add(idx);
    }

    return jsFiles;
  }

  private String computeImports(String pkg, String className, String content) {
    Map<String, Set<String>> baseImportsByFolder = new HashMap<>();
    Set<String> otherImports = new TreeSet<>();
    boolean needsEnsureWasm = false;

    // Match flattened names like base$$iter$$Sum_0, base$$iter$$Sum_0Impl, base$$_InfoToJson_0, test$$Test_0
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
        // figure out folder: e.g. base$$flows$$Foo_0 → base/flows
        String[] parts = dep.split("\\$\\$");
        String folder = "base";
        if (parts.length > 2) {
          folder += "/" + String.join("/", Arrays.copyOfRange(parts, 1, parts.length - 1));
        }
        baseImportsByFolder.computeIfAbsent(folder, k -> new TreeSet<>()).add(dep);
      } else {
        String importPath = getRelativeImportPath(pkg, base);
        otherImports.add("import { " + dep + " } from \"" + importPath + "\";");
      }
    }

    // runtime helpers
    Pattern rt = Pattern.compile("\\brt\\$\\$([A-Za-z0-9_]+)\\b");
    Matcher rm = rt.matcher(content);
    while (rm.find()) {
      String dep = "rt$$" + rm.group(1);
      String importPath = getRelativeImportPath(pkg, "rt-js/" + rm.group(1));
      otherImports.add("import { " + dep + " } from \"" + importPath + "\";");
      if (dep.equals("rt$$NativeRuntime")) needsEnsureWasm = true;
    }

    StringBuilder importBlock = new StringBuilder();

    // Add base imports per folder
    for (var entry : baseImportsByFolder.entrySet()) {
      String folder = entry.getKey();
      Set<String> deps = entry.getValue();

      int depth = pkg.isEmpty() ? 0 : pkg.split("\\.").length;
      StringBuilder prefix = new StringBuilder();
      if (depth == 0) {
        prefix.append("./");
      } else {
        for (int i = 0; i < depth; i++) prefix.append("../");
      }

      importBlock.append("import { ")
        .append(String.join(", ", deps))
        .append(" } from \"")
        .append(prefix).append(folder).append("/index.js\";\n");
    }

    for (String imp : otherImports) {
      importBlock.append(imp).append("\n");
    }

    if (importBlock.length() > 0) importBlock.append("\n");
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
  private JsFile createBaseIndexJs(Map<Id.DecId, List<String>> baseExports, JsCodegen gen) {
    StringBuilder rootIndex = new StringBuilder();

    for (var entry : baseExports.entrySet()) {
      Id.DecId decId = entry.getKey();
      String pkg = decId.pkg();               // e.g. "base.json"
      String typeName = gen.id.getSimpleName(decId); // e.g. "Fear340$_0"

      // Flattened export name: base$$json$$Fear340$_0
      String flattenedName = pkg.replace(".", "$$") + "$$" + typeName;

      // Compute relative import path
      String relPath;
      if (pkg.equals("base")) {
        relPath = "./" + typeName + ".js";
      } else {
        String pkgPath = pkg.replace(".", "/");
        relPath = "./" + pkgPath.substring(5) + "/" + typeName + ".js"; // remove "base/" prefix
      }

      if (gen.freshImpls.containsKey(decId)) {
        // If Impl exists, also export it
        rootIndex.append(
          "export { %s, %s } from '%s';\n".formatted(flattenedName, flattenedName + "Impl", relPath)
        );
      } else {
        // Always export the interface
        rootIndex.append(
          "export { %s } from '%s';\n".formatted(flattenedName, relPath)
        );
      }
    }

    return new JsFile(Path.of("base/index.js"), rootIndex.toString());
  }


  private JsFile createIndexJsForFolder(String folder, Map<Id.DecId, String> exports, JsCodegen gen) {
    StringBuilder sb = new StringBuilder();
    for (var entry : exports.entrySet()) {
      Id.DecId decId = entry.getKey();
      String typeName = gen.id.getSimpleName(decId);
      String flattened = decId.pkg().replace(".", "$$") + "$$" + typeName;
      String relPath = "./" + typeName + ".js"; // always local

      if (gen.freshImpls.containsKey(decId)) {
        sb.append("export { ").append(flattened).append(", ").append(flattened)
          .append("Impl } from '").append(relPath).append("';\n");
      } else {
        sb.append("export { ").append(flattened).append(" } from '").append(relPath).append("';\n");
      }
    }
    return new JsFile(Path.of(folder, "index.js"), sb.toString());
  }


}