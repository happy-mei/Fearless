package magic;

import ast.Program;
import failure.CompileError;
import failure.Fail;
import id.Id;
import utils.Bug;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Magic {
  public static final Id.DecId Main = new Id.DecId("base.Main", 0);
  public static final Id.DecId Sealed = new Id.DecId("base.Sealed", 0);
  public static final Id.DecId Nat = new Id.DecId("base.Nat", 0);
  public static final Id.DecId Int = new Id.DecId("base.Int", 0);
  public static final Id.DecId Bool = new Id.DecId("base.Bool", 0);
  public static final Id.DecId Float = new Id.DecId("base.Float", 0);
  public static final Id.DecId Str = new Id.DecId("base.Str", 0);
  public static final Id.DecId Debug = new Id.DecId("base.Debug", 0);

  public static final Id.DecId BlockK = new Id.DecId("base.Block", 0);
  public static final Id.DecId Block = new Id.DecId("base.Block", 1);
  public static final Id.DecId ReturnStmt = new Id.DecId("base.ReturnStmt", 1);
  public static final Id.DecId Condition = new Id.DecId("base.Condition", 0);
  public static final Id.DecId Continuation = new Id.DecId("base.Continuation", 3);


  public static final Id.DecId RefK = new Id.DecId("base._MagicVarImpl", 0);
  public static final Id.DecId IsoPodK = new Id.DecId("base.caps._MagicIsoPodImpl", 0);
  public static final Id.DecId Assert = new Id.DecId("base.Assert", 0);
  public static final Id.DecId Abort = new Id.DecId("base.Abort", 0);
  public static final Id.DecId MagicAbort = new Id.DecId("base.Magic", 0);
  public static final Id.DecId ErrorK = new Id.DecId("base.Error", 0);
  public static final Id.DecId Try = new Id.DecId("base.Try", 0);
  public static final Id.DecId CapTry = new Id.DecId("base.caps.CapTry", 0);

  public static final Id.DecId FlowK = new Id.DecId("base.flows.Flow", 0);
  public static final Id.DecId FlowOp = new Id.DecId("base.flows.FlowOp", 1);
  public static final Id.DecId PipelineParallelSinkK = new Id.DecId("base.flows._PipelineParallelSink", 0);
  public static final Id.DecId SeqSinkK = new Id.DecId("base.flows._Sink", 0);
  public static final Id.DecId PipelineParallelFlowK = new Id.DecId("base.flows._PipelineParallelFlow", 0);
  public static final Id.DecId FList = new Id.DecId("base.List", 1);
  public static final Id.DecId ListK = new Id.DecId("base.List", 0);
  public static final Id.DecId LList = new Id.DecId("base.LList", 1);
  public static final Id.DecId SafeFlowSource = new Id.DecId("base.flows._SafeSource", 0);

  // object capabilities
  public static final Id.DecId SystemImpl = new Id.DecId("base.caps._System", 0);
  public static final Id.DecId RootCap = new Id.DecId("base.caps.RootCap", 0);
  public static final Id.DecId FIO = new Id.DecId("base.caps.FIO", 0);
  public static final Id.DecId FEnv = new Id.DecId("base.caps.FEnv", 0);
  public static final Id.DecId FRandomSeed = new Id.DecId("base.caps.FRandomSeed", 0);
  public static final List<Id.DecId> ObjectCaps = List.of(
    RootCap,
    FIO,
    FEnv,
    Debug,
    FRandomSeed
  );

  public static Optional<Id.IT<astFull.T>> resolve(String name) {
    var isLiteral  = !name.isEmpty() && isLiteral(name);
    if(isLiteral){ return Optional.of(new Id.IT<>(name, List.of())); }
    return Optional.empty();
//    return switch(name){
//      case noMutHygName -> Optional.of(new Id.IT<>(new Id.DecId(noMutHygName, 0), List.of()));
//      default -> Optional.empty();
//    };
  }

  public static astFull.T.Dec getFullDec(Function<Id.DecId, astFull.T.Dec> resolve, Id.DecId id) {
    var base = _getDec(resolve, id);
    return base.map(b->b.withName(id)).orElse(null);
  }

  public static ast.T.Dec getDec(Function<Id.DecId, ast.T.Dec> resolve, Id.DecId id) {
    var base = _getDec(resolve, id);
    return base.map(b->{
      assert b.lambda().its().size() == 2 : b.lambda(); // instance, kind
      return b.withName(id).withLambda(b.lambda().withITs(List.of(
        new Id.IT<>(id, List.of()),
        b.lambda().its().get(1)
      )));
    }).orElse(null);
  }

  public static Optional<String> getLiteral(Program p, Id.DecId d) {
    if (isLiteral(d.name())) { return Optional.of(d.name()); }
    var supers = p.superDecIds(d);
    return supers.stream().filter(dec->{
      var name = dec.name();
      return isLiteral(name);
    }).map(Id.DecId::name).findFirst();
  }

  public static boolean isLiteral(String name) {
    return isStringLiteral(name) || isNumberLiteral(name);
  }
  public static boolean isStringLiteral(String name) {
    return name.startsWith("\"");
  }
  public static boolean isNumberLiteral(String name) {
    return Character.isDigit(name.charAt(0)) || name.startsWith("-") || name.startsWith("+");
  }

  public static Optional<CompileError> validateLiteral(Id.DecId id) {
    assert isLiteral(id.name());
    try {
      var res = _getDec(_->0, id);
      if (res.isEmpty()) {
        return Optional.of(Fail.syntaxError(id+" is not a valid type name."));
      }
    } catch (CompileError err) {
      return Optional.of(err);
    }
    return Optional.empty();
  }

  private static <T> Optional<T> _getDec(Function<Id.DecId, T> resolve, Id.DecId id) {
    var lit = id.name();
    if (isNumberLiteral(lit)) {
      if (lit.matches("[+-][\\d_]*\\d+$")) {
        return Optional.of(resolve.apply(new Id.DecId("base._IntInstance", 0)));
      }
      if (lit.matches("-?[\\d_]*\\d+\\.[\\d_]*\\d+$")) {
        return Optional.of(resolve.apply(new Id.DecId("base._FloatInstance", 0)));
      }
      if (lit.matches("[\\d_]*\\d+$")) {
        return Optional.of(resolve.apply(new Id.DecId("base._NatInstance", 0)));
      }
      return Optional.empty();
    }
    if (isStringLiteral(lit)) {
      return Optional.of(resolve.apply(new Id.DecId("base._StrInstance", 0)));
    }
    assert !isLiteral(lit);
    return Optional.empty();
  }
}
