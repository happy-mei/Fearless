package tour;

import utils.Base;

import static codegen.java.RunJavaProgramTests.ok;
import static utils.RunOutput.Res;

public class TourHelper {
  private static String lastLine(String text) {
    text=text.trim();
    int lastIndex = text.lastIndexOf("\n");
    return lastIndex == -1 ? text : text.substring(lastIndex + 1);
  }

  public static void run(String content){
    var last= lastLine(content);
    if (!content.startsWith("package")){ content = "package test\n" + content; }
    if (last.startsWith("//prints ")){
      ok(new Res(last.substring("//prints ".length()),"",0),"test.Test",content,Base.mutBaseAliases);
      return;
    }
    //TODO: add case for errs?
    ok(new Res("","",0),"test.Test",content,Base.mutBaseAliases);
  }
}
