package main;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import astFull.T;
import files.Pos;
import utils.Bug;

public class Fail{
  static {
    // Ensure that ErrorCode is consistent
    Arrays.stream(Fail.class.getDeclaredMethods())
      .filter(m-> Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()))
      .filter(m->!m.getName().equals("conflict"))
      .forEach(m->{
        try {
          ErrorCode.valueOf(m.getName());
        } catch (IllegalArgumentException e) {
          throw Bug.of("ICE: ErrorCode enum is not complete.");
        }
      });
  }

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
    int code=ErrorCode.valueOf(kind).code();
    return new CompileError(kind+":"+code+"\n"+msg);
  }

  //ALL OUR ERRORS -- only add to the bottom
  public static CompileError conflictingAlias(String aliased, List<Conflict> conflicts){return of(
      "This alias is in conflict with other aliases in the same package: "+conflictingMsg(aliased, conflicts));}
  public static CompileError conflictingDecl(T.DecId decl, List<Conflict> conflicts){return of(
      "This trait declaration is in conflict with other trait declarations in the same package: "+conflictingMsg(decl.toString(), conflicts));}
  public static CompileError concreteTypeInFormalParams(T badType){return of(
    "Trait and method declarations may only have type parameters. This concrete type was provided instead:\n"+badType
  );}
  public static CompileError reservedIdentifierUsed(String badIdent){return of(
    "This identifier contains reserved text which is reserved for internal compiler usage:\n"+badIdent
  );}
}

//only add to the bottom
enum ErrorCode {
  conflictingAlias,
  conflictingDecl,
  concreteTypeInFormalParams,
  reservedIdentifierUsed;
  int code() {return this.ordinal() + 1;}
}
