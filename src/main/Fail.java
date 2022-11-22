package main;

import astFull.T;
import files.Pos;
import id.Id;
import utils.Bug;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

  //ALL OUR ERRORS
  public static CompileError conflictingAlias(String aliased, List<Conflict> conflicts){return of(
      "This alias is in conflict with other aliases in the same package: "+conflictingMsg(aliased, conflicts));}
  public static CompileError conflictingDecl(Id.DecId decl, List<Conflict> conflicts){return of(
      "This trait declaration is in conflict with other trait declarations in the same package: "+conflictingMsg(decl.toString(), conflicts));}

  public static CompileError conflictingMethArgs(List<String> conflicts){return of(
    "Parameters in methods must have different names. The following parameters were conflicting: " + String.join(", ", conflicts));}

  public static CompileError concreteTypeInFormalParams(T badType){return of(
    "Trait and method declarations may only have generic type parameters. This concrete type was provided instead:\n"+badType
      +"\nAlternatively, are you attempting to shadow an existing class name?"
  );}
  public static CompileError modifierOnInferredLambda(){return of(
    "Modifiers cannot be specified on lambdas without an explicit type."
  );}
  public static CompileError isoInTypeArgs(T badType){return of(
    "The iso reference capability may not be used in type modifiers:\n"+badType
  );}
  public static CompileError shadowingX(String x){return of("Local variable "+x+" is shadowing another variable in scope.");}

  public static CompileError shadowingGX(String x){return of("Type variable "+x+" is shadowing another type variable in scope.");}

  public static CompileError explicitThis(){ return of("Local variables may not be named 'this'."); }

  public static CompileError cyclicImplRelation(Id.DecId baseClass){
    return of(String.format("Implements relations must be acyclic. There is a cycle on the class %s.", baseClass));
  }
  public static CompileError invalidMdf(T t){return of("The modifier 'mdf' can only be used on generic type variables. 'mdf' found on type "+t);}
}

//only add to the bottom
enum ErrorCode {
  conflictingAlias,
  conflictingDecl,
  concreteTypeInFormalParams,
  modifierOnInferredLambda,
  isoInTypeArgs,
  explicitThis,
  conflictingMethArgs,
  cyclicImplRelation,
  shadowingX,
  shadowingGX,
  invalidMdf;
  int code() {return this.ordinal() + 1;}
}
