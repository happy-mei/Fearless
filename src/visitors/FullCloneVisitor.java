package visitors;

import ast.Mdf;
import astFull.E;
import astFull.T;
import id.Id.MethName;

public interface FullCloneVisitor {
  default E.Meth visitMeth(E.Meth e){ return new E.Meth(
    e.sig().map(this::visitSig),
    e.name().map(this::visitMethName),
    e.xs(),
    e.body().map(b->b.accept(this))
  );}
  default E visitMCall(E.MCall e){ return new E.MCall(
    e.receiver().accept(this),
    visitMethName(e.name()),
    e.ts().map(tts->tts.stream().map(this::visitT).toList()),
    e.es().stream().map(ei->ei.accept(this)).toList(),
    visitT(e.t())
  );}
  default E visitX(E.X e){return visitXX(e);}
  default E.X visitXX(E.X e){return e;}
  default E visitLambda(E.Lambda e){ return visitLLambda(e); }
  default E.Lambda visitLLambda(E.Lambda e){ return new E.Lambda(
    visitMdf(e.mdf()),
    e.its().stream().map(this::visitIT).toList(),
    e.selfName(),//visitXX is not ok since this is just a String
    e.meths().stream().map(this::visitMeth).toList(),
    visitT(e.t())
  ); }
  default Mdf visitMdf(Mdf mdf){return mdf;}
  default MethName visitMethName(MethName e){ return e; }
  default E.Sig visitSig(E.Sig e){return new E.Sig(
    visitMdf(e.mdf()),
    e.gens().stream().map(this::visitGX).toList(),
    e.ts().stream().map(this::visitT).toList(),
    visitT(e.ret())
  );}
  default T visitT(T t){ return new T(
    visitMdf(t.mdf()),
    t.rt().match(this::visitGX,this::visitIT)
  );}
  default T.IT visitIT(T.IT t){ return new T.IT(
    t.name(),
    t.ts().stream().map(this::visitT).toList()
  );}
  default T.GX visitGX(T.GX t){ return t; }
  default T.Dec visitDec(T.Dec d) { return new T.Dec(
    d.name(),
    d.gxs().stream().map(this::visitGX).toList(),
    visitLLambda(d.lambda())
  );}
  default T.Alias visitAlias(T.Alias a){ return new T.Alias(
    visitIT(a.from()),
    a.to()
  ); }
}