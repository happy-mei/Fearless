package visitors;

import astFull.E;
import astFull.T;
import id.Id;

import java.util.HashSet;
import java.util.Set;

public class FreeGensFullVisitor implements FullCloneVisitor {
  public final Set<Id.GX<T>> freeGens = new HashSet<>();
  private Set<Id.GX<T>> fresh = new HashSet<>();

//  @Override public E.Lambda visitLLambda(E.Lambda e) {
////    var old = fresh;
////    fresh = new HashSet<>(fresh);
////    fresh.addAll(e.name().gens());
//    var res = FullCloneVisitor.super.visitLLambda(e);
////    this.fresh = old;
//    return res;
//  }

  @Override public E.Sig visitSig(E.Sig e) {
    var old = fresh;
    fresh = new HashSet<>(fresh);
    fresh.addAll(e.gens());
    var res = FullCloneVisitor.super.visitSig(e);
    this.fresh = old;
    return res;
  }

  @Override public E.Meth visitMeth(E.Meth e) {
    var old = fresh;
    fresh = new HashSet<>(fresh);
    e.sig().ifPresent(sig->fresh.addAll(sig.gens()));
    var res = FullCloneVisitor.super.visitMeth(e);
    this.fresh = old;
    return res;
  }

  @Override public Id.GX<T> visitGX(Id.GX<T> t) {
    if (!fresh.contains(t)) { freeGens.add(t); }
    return FullCloneVisitor.super.visitGX(t);
  }

  @Override public T visitT(T t) {
    if (t.isInfer()) { return t; }
    return FullCloneVisitor.super.visitT(t);
  }
}
