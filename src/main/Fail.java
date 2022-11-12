package main;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import astFull.T;
import files.Pos;
public class Fail{
  public record Conflict(Pos pos,String descr){}
  public static Conflict conflict(Pos pos,String descr){ return new Conflict(pos,descr); }
  private static String conflictingMsg(String base, List<Conflict> conflicts){
    var conflictMsg = conflicts.stream()
      .map(c->String.format("(%s) %s", c.pos(), c.descr()))
      .collect(Collectors.joining("\n"));

    return String.format("%s\nconflicts:\n%s", base, conflictMsg);
    }
  private static CompileError of(String msg){
    var arr=Thread.currentThread().getStackTrace();
    var kind=arr[2].getMethodName();
    var ms=Stream.of(Fail.class.getDeclaredMethods())
      .sorted(Comparator.comparing(Method::getName))
      .filter(m->Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()))
      .toList();
    int code=IntStream.range(0, ms.size())
      .filter(i->ms.get(i).getName().equals(kind))
      .findFirst()
      .getAsInt();
    return new CompileError(kind+":"+code+"\n"+msg);
  }
  
  //ALL OUR ERRORS -- only add to the bottom
  public static CompileError conflictingAlias(String aliased, List<Conflict> conflicts){return of(
      "This alias is in conflict with other aliases in the same package: "+conflictingMsg(aliased, conflicts));}

  public static CompileError conflictingDecl(T.DecId decl, List<Conflict> conflicts){return of(
      "This trait declaration is in conflict with other trait declarations in the same package: "+conflictingMsg(decl.toString(), conflicts));}
}
