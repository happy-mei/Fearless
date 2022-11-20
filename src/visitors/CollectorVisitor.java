package visitors;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import ast.E;
import ast.Mdf;
import ast.T;

public interface CollectorVisitor<R>{
  default Optional<R> visitMeth(E.Meth e){
    return visitSig(e.sig())
      .or(()->visitMethName(e.name()))
      .or(()->e.body().flatMap(b->b.accept(this)));
  }
  static <R,ET> Optional<R> visitAll(List<ET> ts, Function<ET,Optional<R>> f){
    for(var e:ts){
      var r=f.apply(e);
      if(r.isPresent()){ return r; }
    }
    return Optional.empty();
  }
  default Optional<R> visitMCall(E.MCall e){
    return e.receiver().accept(this)
      .or(()->visitMethName(e.name()))
      .or(()->visitAll(e.ts(),this::visitT))
      .or(()->visitAll(e.es(),ei->ei.accept(this)));
  }
  default Optional<R> visitX(E.X e){ return Optional.empty(); }
  default Optional<R> visitLambda(E.Lambda e){
    return visitMdf(e.mdf())
      .or(()->visitAll(e.its(),this::visitIT))
      .or(()->visitAll(e.meths(),this::visitMeth));
  }
  default Optional<R> visitMdf(Mdf mdf){return Optional.empty();}
  default Optional<R> visitMethName(E.MethName e){ return Optional.empty(); }
  default Optional<R> visitSig(E.Sig e){
    return visitMdf(e.mdf())
      .or(()->visitAll(e.gens(),this::visitGX))
      .or(()->visitT(e.ret()));
  }
  default Optional<R> visitT(T t){
    return visitMdf(t.mdf())
      .or(()->t.rt().match(this::visitGX,this::visitIT));
  }
  default Optional<R> visitIT(T.IT t){ return visitAll(t.ts(),this::visitT); }
  default Optional<R> visitGX(T.GX t){ return Optional.empty(); }
  default Optional<R> visitDec(T.Dec d){
    return visitAll(d.gxs(),this::visitGX).or(()->visitLambda(d.lambda()));
  }
  default Optional<R> visitDecId(T.DecId di){ return Optional.empty(); }
}