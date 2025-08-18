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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    List<JsFile> magicFiles =main.io().magicJsFiles();
    assert !magicFiles.isEmpty() : "Failed to read magic files";
    jsFiles.addAll(magicFiles);
    return Collections.unmodifiableList(jsFiles);
  }
  public ArrayList<JsFile> generateFiles() {
    var jsFiles = new ArrayList<JsFile>();
    var gen = new JsCodegen(program);

    for (MIR.Package pkg : program.pkgs()) {
      if (main.cachedPkg().contains(pkg.name())) continue;

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
      }
    }

    // Handle generated implementation classes
//      for (var e : gen.freshClasses.entrySet()) {
//        String implFileName = gen.id.getSimpleName(e.getKey()) + "Impl.js";
//        Path implFilePath = Path.of(pkgPath).resolve(implFileName);
//        jsFiles.add(new JsFile(implFilePath, e.getValue()));
//      }
    return jsFiles;
  }

  private JsFile createJsFile(Path p, String content) {
    return new JsFile(p, content);
  }
}
