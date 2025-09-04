package codegen.js;

import codegen.MIR;
import main.js.LogicMainJs;
import utils.IoErr;
import utils.ResolveResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public record JsProgram(List<JsFile> files) {
  public JsProgram(LogicMainJs main, MIR.Program program) {
    this(new ToJsProgram(main, program).of());
  }

  public void writeJsFiles(Path output) {
    IoErr.of(() -> _writeJsFiles(output));
  }

  private void _writeJsFiles(Path output) throws IOException {for (var file : files) {
    Path fullPath = output.resolve(file.path());

    // Special handling for WASM files
    if (file.path().startsWith("rt/libwasm")) {
      Files.createDirectories(fullPath.getParent());
      Files.write(fullPath, file.code().getBytes(StandardCharsets.ISO_8859_1),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
      continue;
    }

    // Normal JS files
    Files.createDirectories(fullPath.getParent());
    Files.write(fullPath, file.code().getBytes(),
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING);
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

    for (MIR.Package pkg : program.pkgs()) {
//      if (main.cachedPkg().contains(pkg.name())){ continue; } // No caching class files for JS
      String pkgPath = pkg.name().replace(".", "/") + "/";
      for (MIR.TypeDef def : pkg.defs().values()) {
        var funsList = pkg.funs().stream()
          .filter(f -> f.name().d().equals(def.name()))
          .toList();
        String code = gen.visitTypeDef(pkg.name(), def, funsList);
        if (code.isEmpty()) continue;

        String fileName = gen.id.getSimpleName(def.name()) + ".js";
        Path filePath = Path.of(pkgPath).resolve(fileName);

        jsFiles.add(new JsFile(filePath, code));

        // Track base.* for merging
        if (pkg.name().startsWith("base")) {
          baseExports
            .computeIfAbsent(pkg.name(), k -> new ArrayList<>())
            .add(gen.id.getSimpleName(def.name()));
        }
      }
    }

    // Now generate merged base/index.js
    if (!baseExports.isEmpty()) {
      JsFile baseIndexJs = getBaseIndexJs(baseExports);
      jsFiles.add(baseIndexJs);
    }

    return jsFiles;
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
