package main;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import files.Pos;
public class Fail{
  record Conflict(Pos pos,String descr){}
  public static Conflict conflict(Pos pos,String descr){ return new Conflict(pos,descr); }
  private static String conflictingMsg(String base, List<Conflict> conflicts){
    var conflictMsg = conflicts.stream()
      .map(c->String.format("(%d:%d) %s", c.pos.line(), c.pos().column(), c.descr()))
      .collect(Collectors.joining("\n"));

    return String.format("There was a conflict between:\n%s\nand the following items:\n", base, conflictMsg);
    }
  private static CompileError of(String msg){
    var arr=Thread.currentThread().getStackTrace();
    var kind=arr[1].getMethodName();
    var ms=Stream.of(Fail.class.getDeclaredMethods()).filter(m->
      Modifier.isStatic(m.getModifiers())
      && Modifier.isPublic(m.getModifiers()))
      .toList();
    int code=IntStream.range(0, ms.size())
      .filter(i->ms.get(i).getName().equals(kind)).findFirst().getAsInt();
    return new CompileError(kind+":"+code+"\n"+msg);
  }
  
  //ALL OUR ERRORS -- only add to the bottom
  public static CompileError conflictingAlias(String aliased, List<Conflict> conflicts){return of(
      "Two aliases are in conflict:"+conflictingMsg(aliased, conflicts));}  
}
