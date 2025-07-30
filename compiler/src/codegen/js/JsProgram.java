package codegen.js;

import codegen.MIR;
import main.js.LogicMainJs;
import utils.IoErr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record JsProgram(List<JsFile> files) {
  public JsProgram(LogicMainJs main, MIR.Program program) {
    this(new ToJsProgram(main, program).generateFiles());
  }

  public void writeJsFiles(Path output) {
    IoErr.of(() -> _writeJsFiles(output));
  }

  private void _writeJsFiles(Path output) throws IOException {
    for (var file : files) {
      // Combine package path + filename into full path
      Path filePath = output.resolve(file.pkgPath()).resolve(file.name());

      // Ensure *parent* directories exist (works even if pkgPath has slashes)
      Files.createDirectories(filePath.getParent());

      Files.write(filePath, file.code().getBytes());
    }
  }
}

record ToJsProgram(LogicMainJs main, MIR.Program program) {
  public List<JsFile> generateFiles() {
    var jsFiles = new ArrayList<JsFile>();
    var gen = new JsCodegen(program);

    for (MIR.Package pkg : program.pkgs()) {
      if (main.cachedPkg().contains(pkg.name())) continue;

      for (MIR.TypeDef def : pkg.defs().values()) {
        // Find all functions whose name matches def's name
        var funs = pkg.funs().stream()
          .filter(f -> f.name().d().equals(def.name()))
          .toList();
        String code = gen.visitTypeDef(pkg.name(), def, funs);
        if (code.isEmpty()) continue;
        String simpleName = gen.id.getSimpleName(def.name()); // → Test_0
        String fileName = simpleName + ".js";                 // → Test_0.js
        String filePath = pkg.name().replace(".", "/");
        JsFile jsFile = createJsFile(filePath, fileName, code);
        jsFiles.add(jsFile);
      }
    }
    // Add any generated classes (from visitCreateObjNoSingleton)
    for (var e : gen.freshClasses.entrySet()) {
      var decId = e.getKey();
      String pkg = e.getKey().pkg();
      String name = gen.id.getSimpleName(decId) + "Impl.js";
      String filePath = pkg.replace(".", "/");
      String content = e.getValue();
      JsFile classFile = createJsFile(filePath, name, content);
      jsFiles.add(classFile);
    }

    return Collections.unmodifiableList(jsFiles);
  }

  private JsFile createJsFile(String pkgPath, String fileName, String content) {
    return new JsFile(pkgPath, fileName, content);
  }
}

record JsFile(String pkgPath, String fileName, String code) {
  public String name() { return fileName; }

  public Object toUri() {
    var path = Path.of(pkgPath, fileName);
    return path.toUri();
  }
}
