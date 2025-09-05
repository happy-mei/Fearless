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

    // First pass: generate all interfaces and collect Impl classes
    for (MIR.Package pkg : program.pkgs()) {
      String pkgPath = pkg.name().replace(".", "/") + "/";
      for (MIR.TypeDef def : pkg.defs().values()) {
        Id.DecId typeId = def.name();
        var funsList = pkg.funs().stream()
          .filter(f -> f.name().d().equals(typeId))
          .toList();

        // Generate interface
        String typeDefContent = gen.visitTypeDef(def, funsList);
        if (!typeDefContent.isEmpty()) {
          String fileName = gen.id.getSimpleName(typeId) + ".js";
          Path filePath = Path.of(pkgPath).resolve(fileName);
          String className = gen.id.getFullName(typeId);
          String importLines = computeImports(pkg.name(), className, typeDefContent);
          jsFiles.add(new JsFile(filePath, importLines + typeDefContent));

          // Track base.* for merged index
          if (pkg.name().startsWith("base")) {
            baseExports.computeIfAbsent(pkg.name(), k -> new ArrayList<>())
              .add(gen.id.getSimpleName(def.name()));
          }
        }
      }
    }

    // Second pass: generate all Impl classes
    for (var e : gen.freshImpls.entrySet()) {
      Id.DecId typeId = e.getKey();
      String pkgName = typeId.pkg();
      String fileName = gen.id.getSimpleName(typeId) + "Impl.js";
      Path filePath = Path.of(pkgName.replace(".", "/")).resolve(fileName);
      String className = gen.id.getFullName(typeId) + "Impl";
      String importLines = computeImports(pkgName, className, e.getValue());
      String content = importLines + e.getValue();
      jsFiles.add(new JsFile(filePath, content));
    }

    // Now generate merged base/index.js
    if (!baseExports.isEmpty()) {
      JsFile baseIndexJs = getBaseIndexJs(baseExports);
      jsFiles.add(baseIndexJs);
    }

    return jsFiles;
  }

  private String computeImports(String pkg, String className, String content) {
    Set<String> imports = new HashSet<>();
    // Pattern matches full-encoded names:
    //   <pkg>$$<pkg>$$...$$<TypeOr_underscoreStart>_<digits>  (optionally followed by Impl)
    // Examples matched:
    //   base$$iter$$Sum_0
    //   base$$iter$$Sum_0Impl
    //   base$$_InfoToJson_0
    //   test$$Test_0
    //   test$$Test_0Impl
    Pattern p = Pattern.compile(
      "([a-z][a-z0-9_]*(?:\\$\\$[a-z][a-z0-9_]*)*\\$\\$[A-Za-z_][A-Za-z0-9_$]*_\\d+(?:Impl)?)"
    );
    Matcher m = p.matcher(content);

    while (m.find()) {
      String dep = m.group();
      if (dep.equals(className)) continue; // skip self
      String importPath = getRelativeImportPath(pkg, dep);
      imports.add("import { " + dep + " } from \"" + importPath + "\";");
    }

    if (!pkg.equals("rt") && content.contains(" rt.")) {
      imports.add("import * as rt from \"../rt-js/index.js\";");
    }
    return String.join("\n", imports) + (imports.isEmpty() ? "" : "\n\n");
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

  private JsFile getBaseIndexJs(Map<String, List<String>> baseExports) {
    StringBuilder rootIndex = new StringBuilder();

    for (var entry : baseExports.entrySet()) {
      String pkg = entry.getKey(); // e.g., "base", "base.json"
      List<String> typeNames = entry.getValue();

      for (String typeName : typeNames) {
        // Compute flattened export name: base__json__Type_0
        String flattenedName = pkg.replace(".", "__") + "__" + typeName;

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