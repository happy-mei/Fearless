package program.typesystem;

import ast.E;
import ast.T;
import failure.CompileError;
import failure.Res;
import id.Mdf;
import program.CM;
import program.TypeRename;
import utils.Bug;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static program.typesystem.EMethTypeSystem.recvPriority;

/*
#define guessType(G,e)=C[Ts]

guessType(G,x)=G(x).ITX
guessType(G,B) = B.IT0? name?
guessType(G,e.m(e1..en))=T.ITX
  guessType(G,e)=C[Ts]
  Ds(C[Ts]).m/n = T1..Tn:T //take the highest priority on overloading?
//helps guessing a better expected type for the receiver of a m-call
//usage:
e0.m(e1..en)
  first call guessType(e0) = C[Ts]
  then Ds(C[Ts]).m/n = mdf C[Ts] T1..Tn->T
now use mdf C[Ts] as the expected type to type e0
 */

public interface GuessType extends ETypeSystem {
  default Res guessRecvType(E.MCall e) {
    List<CM> cms;
    if (e.receiver() instanceof E.Lambda l) {
      var tmpDec = T.Dec.ofComposite(l.its());
      cms = p().withDec(tmpDec).meths(xbs(), Mdf.recMdf, tmpDec.toIT(), e.name(), depth());
    } else {
      var recv = e.receiver().accept(this).t();
      if (recv.isEmpty()) { return new CompileError("Failed to guess type of: "+e); }
      cms = p().meths(xbs(), Mdf.recMdf, recv.get().itOrThrow(), e.name(), depth());
    }

//    var cm = cms.stream()
//      .min(Comparator.comparingInt(cm_->recvPriority.indexOf(cm_.mdf())))
//      .orElseThrow();
    if (cms.size() != 1) { return new CompileError("Failed to guess type of: "+e); }
    var cm = cms.get(0);
    return new T(cm.mdf(), cm.c());
  }

  @Override default Res visitX(E.X e) {
    return g().get(e).withMdf(Mdf.mdf);
  }
  @Override default Res visitLambda(E.Lambda e) {
    throw Bug.unreachable();
  }
  @Override default Res visitMCall(E.MCall e) {
    List<CM> cms;
    if (e.receiver() instanceof E.Lambda l) {
      var tmpDec = T.Dec.ofComposite(l.its());
      cms = p().withDec(tmpDec).meths(xbs(), Mdf.recMdf, tmpDec.toIT(), e.name(), depth());
    } else {
      var recv = e.receiver().accept(this).t();
      if (recv.isEmpty()) { return new CompileError("Failed to guess type of: "+e); }
      cms = p().meths(xbs(), Mdf.recMdf, recv.get().itOrThrow(), e.name(), depth());
    }
    var cm = cms.stream()
      .min(Comparator.comparingInt(cm_->recvPriority.indexOf(cm_.mdf())))
      .orElseThrow();
//    if (cms.size() != 1) { return new CompileError("Failed to guess type of: "+e); }
//    var cm = cms.get(0);
    var renamer = TypeRename.core(p());
    var sig = renamer.renameSigOnMCall(cm.sig(), xbs().addBounds(cm.sig().gens(), cm.bounds()), renamer.renameFun(e.ts(), cm.sig().gens()));
    return sig.ret().withMdf(Mdf.mdf);
  }
}
