package magic;

import failure.Fail;
import id.Id;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import ast.E.Lambda;
import ast.E.Lambda.LambdaId;
import ast.T.Dec;

public class Magic {
  public static final Id.DecId Main = new Id.DecId("base.Main", 0);
  public static final Id.DecId Sealed = new Id.DecId("base.Sealed", 0);
  public static final Id.DecId UInt = new Id.DecId("base.UInt", 0);
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


  public static final Id.DecId RefK = new Id.DecId("base._MagicRefImpl", 0);
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
    var isLiteral  = !name.isEmpty() && MagicImpls.isLiteral(name);
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
  public static Dec getDecMap(Dec b, Id.DecId id){
    Lambda l= b.lambda();
    LambdaId lid= l.id();
    assert lid.id().name().endsWith("Instance");
    assert l.its().size() == 1 : l; 
    // instance, kind   0.5  anon:base._FloatInstance, base.Float
    var its= List.of(lid.toIT(),l.its().get(0));
    l = l.withId(lid.withId(id)).withITs(its);
    return b.withLambda(l);
  }
  public static Dec getDec(Function<Id.DecId, Dec> resolve, Id.DecId id) {
    var base = _getDec(resolve, id);
    return base.map(b->getDecMap(b,id)).orElse(null);
  }

  //TODO: refactor for mandatory +/- and no 'u'.
  private static boolean nameIsNumber(String name){
    return Character.isDigit(name.charAt(0)) || name.startsWith("-");
  }
  private static boolean nameIsString(String name){
    return name.charAt(0) == '"';
  }

  private static <T> Optional<T> _getDec(Function<Id.DecId,T> resolve, Id.DecId id) {
    if(id.gen() != 0){ return Optional.empty(); }
    if(nameIsString(id.name())){ 
      return Optional.of(resolve.apply(new Id.DecId("base._StrInstance", 0)));
    }
    if (!nameIsNumber(id.name())){ return Optional.empty(); }
    T baseDec= null;
    var nDots = id.name().chars().filter(c->c=='.').limit(2).count();
    if (nDots > 1) { throw Fail.invalidNum(id.name(), "Float"); }
    if (id.name().endsWith("u")) {
      assert nDots==0; //u implies no dots
      baseDec = resolve.apply(new Id.DecId("base._UIntInstance", 0));
    }    
    if (nDots > 0) {
      baseDec = resolve.apply(new Id.DecId("base._FloatInstance", 0));
    } 
    if(baseDec == null){
      baseDec = resolve.apply(new Id.DecId("base._IntInstance", 0));
    }
    return Optional.of(baseDec);
  }
}
