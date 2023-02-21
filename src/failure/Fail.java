package failure;

import astFull.E;
import astFull.T;
import files.Pos;
import id.Id;
import id.Mdf;
import program.CM;
import utils.Bug;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
          throw Bug.of("ICE: ErrorCode enum is not complete. Missing: " + m.getName());
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
    return new CompileError("[E"+code+" "+kind+"]\n"+msg);
  }

  //ALL OUR ERRORS
  public static CompileError conflictingAlias(String aliased, List<Conflict> conflicts){return of(
      "This alias is in conflict with other aliases in the same package: "+conflictingMsg(aliased, conflicts));}
  public static CompileError conflictingDecl(Id.DecId decl, List<Conflict> conflicts){return of(
      "This trait declaration is in conflict with other trait declarations in the same package: "+conflictingMsg(decl.toString(), conflicts));}

  public static CompileError uncomposableMethods(List<Conflict> conflicts) { return of(conflictingMsg("These methods could not be composed.", conflicts)); }

  public static CompileError conflictingMethParams(List<String> conflicts){return of(
    "Parameters on methods must have different names. The following parameters were conflicting: " + String.join(", ", conflicts));}

  public static CompileError conflictingMethNames(List<String> conflicts){return of(
    "Methods may not have the same name and number of parameters. The following methods were conflicting: " + String.join(", ", conflicts));}

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
  public static CompileError shadowingX(String x){return of(String.format("'%s' is shadowing another variable in scope.", x));}

  public static CompileError shadowingGX(String x){return of("Type variable "+x+" is shadowing another type variable in scope.");}

  public static CompileError explicitThis(){ return of("Local variables may not be named 'this'."); }

  public static CompileError cyclicImplRelation(Id.DecId baseClass){
    return of(String.format("Implements relations must be acyclic. There is a cycle on the class %s.", baseClass));
  }
  public static CompileError invalidMdf(T t){return of("The modifier 'mdf' can only be used on generic type variables. 'mdf' found on type "+t);}
  public static CompileError invalidMdf(Id.IT<T> it){return of("The modifier 'mdf' can only be used on generic type variables. 'mdf' found on type "+it);}

  public static CompileError concreteInNoMutHyg(T t){return of("The type parameters to NoMutHyg must be generic and present in the type parameters of the trait implementing it. A concrete type was found:\n" + t);}
  public static CompileError invalidNoMutHyg(T t){return of("The type parameters to NoMutHyg must be generic and present in the type parameters of the trait implementing it. This generic type is not a type parameter of the trait:\n" + t);}
  public static CompileError expectedConcreteType(T t){ return of("A concrete type was expected but the following generic type was given:\n" + t); }

  public static CompileError missingDecl(Id.DecId d){ return of("The following trait cannot be aliased because it does not exist:\n"+d); }

  public static CompileError invalidMethMdf(E.Sig s, Id.MethName n){ return of(String.format("%s is not a valid modifier for a method (on the method %s).", s.mdf(), n)); }
  public static CompileError cannotInferSig(Id.DecId d, Id.MethName m){ return of(String.format("Could not infer the signature for %s in %s.", m, d)); }
  public static CompileError cannotInferAbsSig(Id.DecId d){ return of(String.format("Could not infer the signature for the abstract lambda in %s. There must be one abstract lambda in the trait.", d)); }
  public static CompileError traitNotFound(Id.DecId d){ return of(String.format("The trait %s could not be found.", d)); }
  public static CompileError inferFailed(astFull.E e){ return of(String.format("Could not infer the type for the following expression:\n%s", e)); }

  public static CompileError methTypeError(ast.T expected, ast.T actual, Id.MethName m){
    return of(String.format("Expected the method %s to return %s, got %s.", m, expected, actual));
  }
  public static CompileError unimplementedInLambda(List<CM> ms){
    var unimplemented = ms.stream()
      .map(m->"("+m.pos()+") "+m.name())
      .collect(Collectors.joining("\n"));
    return of(String.format("The lambda must implement the following methods:\n%s", unimplemented));
  }

  public static CompileError circularSubType(ast.T t1, ast.T t2){
    return of(String.format("There is a cyclical sub-typing relationship between "+t1+" and "+t2+"."));
  }

  public static CompileError recMdfInNonHyg(Mdf mdf, Id.MethName m, ast.T t){
    return of("Invalid modifier for "+t+".\nrecMdf may only be used in read or lent methods. The method "+m+" has the "+mdf+" modifier.");
  }
  public static CompileError recMdfInNonHyg(Mdf mdf, Id.MethName m, ast.E.Lambda e){
    return of("Invalid lambda modifier.\nrecMdf may only be used in read or lent methods. The method "+m+" has the "+mdf+" modifier.");
  }
  public static CompileError recMdfInImpls(ast.T t){
    return of("Invalid modifier for "+t+".\nrecMdf may not be used in the list of implemented traits.");
  }
  public static CompileError undefinedName(String name){
    return of("The identifier \""+name+"\" is undefined.");
  }
  public static <TT> CompileError noDupImpls(List<Id.IT<TT>> its){
    var dups = its.stream().map(Id.IT::name)
      .collect(Collectors.groupingBy(d->d))
      .entrySet().stream()
      .filter(kv->kv.getValue().size() > 1)
      .map(d->d.getKey().toString())
      .collect(Collectors.joining("\n"));
    return of("The following traits are implemented more than once:\n"+dups+"\nA trait may only be listed once regardless of type parameters.");
  }

  public static CompileError badCapture(String name) {
    return of("The identifier '" + name + "' cannot be captured by this lambda.");
  }
}

//only add to the bottom
enum ErrorCode {
  conflictingAlias,
  conflictingDecl,
  concreteTypeInFormalParams,
  modifierOnInferredLambda,
  isoInTypeArgs,
  explicitThis,
  conflictingMethParams,
  cyclicImplRelation,
  shadowingX,
  shadowingGX,
  invalidMdf,
  concreteInNoMutHyg,
  invalidNoMutHyg,
  expectedConcreteType,
  missingDecl,
  invalidMethMdf,
  conflictingMethNames,
  uncomposableMethods,
  cannotInferSig,
  traitNotFound,
  inferFailed,
  cannotInferAbsSig,
  methTypeError,
  unimplementedInLambda,
  circularSubType,
  recMdfInNonHyg,
  recMdfInImpls,
  undefinedName,
  noDupImpls,
  badCapture;
  int code() {return this.ordinal() + 1;}
}
