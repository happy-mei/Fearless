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
    var aliases = Base.mutBaseAliases;
    if (!content.startsWith("package")){
      content = "package test\n" + content;
      aliases = "package test\n";
    }
    String expectedPrint= "";
    if (last.startsWith("//prints ")){
      expectedPrint = last.substring("//prints ".length()); 
    } 
    if(!content.contains(":Main")){
      content += "Test:Main{s->Void}"; 
    }
    ok(new Res(expectedPrint,"",0), "test.Test",
      content, Base.mutBaseAliases);
    //TODO: add case for errs?
  }
}
