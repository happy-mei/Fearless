package rt.fs;

import base.Infos_0;
import base.List_1;
import base.caps.ReadPath_0;
import base.caps.ReadWritePath_0;
import base.caps.WritePath_0;
import rt.Fallible;
import rt.FearlessError;
import rt.IO;
import rt.Str;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public record ReadWritePath(Path inner) implements ReadWritePath_0 {
  public static ReadPath_0 readPath(ReadWritePath_0 rwPath) {
    return new ReadPath_0() {
      @Override public ReadPath_0 accessR$mut(List_1 path) {
        return rwPath.accessR$mut(path);
      }
      @Override public base.Action_1 readStr$mut() {
        return rwPath.readStr$mut();
      }
      @Override public ReadPath_0 self$mut() {
        return this;
      }
      @Override public ReadPath_0 iso$mut() {
        return this;
      }
    };
  }
  public static WritePath_0 writePath(ReadWritePath_0 rwPath) {
    return new WritePath_0() {
      @Override public WritePath_0 accessW$mut(List_1 path) {
        return rwPath.accessW$mut(path);
      }
      @Override public WritePath_0 self$mut() {
        return this;
      }
      @Override public WritePath_0 iso$mut() {
        return this;
      }
    };
  }

  @Override public ReadWritePath_0 accessRW$mut(List_1 path) {
    var start = inner;
    var res = IO.strListToPath(start, path);
    if (res.startsWith(start)) {
      return new ReadWritePath(res);
    }
    var msg = "Expected the path to be scoped under '" + start + "', but it resolved to '" + res + "'";
    throw new FearlessError(Infos_0.$self.msg$imm(Str.fromJavaStr(msg)));
  }
  @Override public ReadPath_0 accessR$mut(List_1 path) {
    return readPath(this.accessRW$mut(path));
  }
  @Override public WritePath_0 accessW$mut(List_1 path) {
    return writePath(this.accessRW$mut(path));
  }

  @Override public Fallible readStr$mut() {
    return m -> {
      try {
        var uncheckedUtf8 = Str.wrap(Files.readAllBytes(inner));
        return m.ok$mut(Str.fromUtf8(uncheckedUtf8));
      } catch (FearlessError e) {
        return m.info$mut(e.info);
      } catch (FileNotFoundException | NoSuchFileException e) {
        var msg = "File not found: " + e.getMessage();
        return m.info$mut(Infos_0.$self.msg$imm(Str.fromJavaStr(msg)));
      } catch (IOException e) {
        return m.info$mut(Infos_0.$self.msg$imm(Str.fromJavaStr(e.getMessage())));
      }
    };
  }

  @Override public ReadWritePath iso$mut() {
    return this;
  }
  @Override public ReadWritePath self$mut() {
    return this;
  }
}
