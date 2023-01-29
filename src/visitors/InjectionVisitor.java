package visitors;

import ast.T;
import astFull.E;
import id.Id;
import id.Mdf;
import main.CompileError;
import utils.Bug;
import utils.Push;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InjectionVisitor implements FullVisitor<ast.E>{
  public ast.E.MCall visitMCall(E.MCall e) {
    // TODO: WHYYYYYY
    /*
    [-imm base.Let[]-][base.Let[]]{ }
     #/1[infer, imm base.Void[]]([[-imm base.Let[infer, imm base.Void[]]-][]{
     .var/0([]): Sig[mdf=imm,gens=[],ts=[],ret=infer]
        -> this:infer.swap/1[-]([x:infer]):infer,
     .in/1([_]): [-]
       -> [-imm base.Void[]-][base.Void[]]{ }}]):imm base.Void[]

       // old (better):
       [-imm base.Let[]-][base.Let[]]{ } #/1[infer, imm base.Void[]]([[-imm base.Let[infer, imm base.Void[]]-][]{
         .var/0([]): Sig[mdf=imm,gens=[],ts=[],ret=mdf X]
            -> this:mut base.Ref[mdf X].swap/1[]([x:mdf X]):mdf X,
         .in/1([_]): [-]
           -> [-imm base.Void[]-][base.Void[]]{ }}]):imm base.Void[]

       super new:
       [-imm base.Let[]-][base.Let[]]{ } #/1[infer, imm base.Void[]]([[-imm base.Let[infer, imm base.Void[]]-][]{
       .var/0([]): Sig[mdf=imm,gens=[],ts=[],ret=infer] ->
          this:mut base.Ref[mdf X] .swap/1[]([x:mdf X]):mdf X,
        .in/1([_]): Sig[mdf=imm,gens=[],ts=[infer],ret=infer] ->
          [-imm base.Void[]-][base.Void[]]{ }}]):imm base.Void[]

      pls pls pls:
      [-imm base.Let[]-][base.Let[]]{ }
        #/1[infer, imm base.Void[]]([[-imm base.Let[infer, imm base.Void[]]-][]{
          .var/0([]): Sig[mdf=imm,gens=[],ts=[],ret=infer] ->
              this:mut base.Ref[mdf X] .swap/1[]([x:mdf X]):mdf X,
          .in/1([_]): Sig[mdf=imm,gens=[],ts=[infer],ret=infer] ->
            [-imm base.Void[]-][base.Void[]]{ }}]):imm base.Void[]

      putting FearN$ on the right in all RPs:
      [-imm base.Let[]-][base.Let[]]{ } #/1[infer, imm base.Void[]]([[-imm base.Let[infer, imm base.Void[]]-][]{
        .var/0([]): Sig[mdf=imm,gens=[],ts=[],ret=mdf X] ->
          this:mut base.Ref[mdf X] .swap/1[]([x:mdf X]):mdf X,
        .in/1([_]): Sig[mdf=imm,gens=[],ts=[infer],ret=infer] ->
          [-imm base.Void[]-][base.Void[]]{ }}]):imm base.Void[]

      latest nick status:
      [-imm base.Let[]-][base.Let[]]{ } #/1[infer, imm base.Void[]]([[-imm base.Let[infer, imm base.Void[]]-][]{
        .var/0([]): Sig[mdf=imm,gens=[],ts=[],ret=mdf X] ->
          this:mut base.Ref[mdf X] .swap/1[]([x:mdf X]):mdf X,
        .in/1([_]): Sig[mdf=imm,gens=[],ts=[infer],ret=infer] ->
          [-imm base.Void[]-][base.Void[]]{ }}]):imm base.Void[]
     */
    return new ast.E.MCall(
      e.receiver().accept(this),
      e.name(),
      e.ts().orElseThrow().stream().map(this::visitT).toList(),
      e.es().stream().map(ei->ei.accept(this)).toList(),
      e.pos()
    );
  }

  public ast.E.X visitX(E.X e){
    return new ast.E.X(e.name(), e.pos());
  }

  public ast.E.Lambda visitLambda(E.Lambda e){
    return new ast.E.Lambda(
      e.mdf().orElseThrow(),
      e.its().stream().map(this::visitIT).toList(),
      Optional.ofNullable(e.selfName()).orElseGet(E.X::freshName),
      e.meths().stream().map(this::visitMeth).toList(),
      e.pos()
    );
  }

  public ast.T.Dec visitDec(astFull.T.Dec d){
    return new ast.T.Dec(
      d.name(),
      d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList(),
      this.visitLambda(d.lambda().withMdf(Mdf.mdf).withITs(Push.of(d.toIT(), d.lambda().its()))),
      d.pos()
    );
  }

  public ast.T visitT(astFull.T t){
    if (t.isInfer()) {
      // TODO: throw Fail.....
      throw Bug.todo();
    }
    return t.toAstT();
  }

  public Id.IT<ast.T> visitIT(Id.IT<astFull.T> t){
    return new Id.IT<>(
      t.name(),
      t.ts().stream().map(this::visitT).toList()
    );
  }

  public ast.E.Meth visitMeth(E.Meth m){
    return new ast.E.Meth(
      visitSig(m.sig().orElseThrow()),
      m.name().orElseThrow(),
      m.xs(),
      m.body().map(b->b.accept(this)),
      m.pos()
    );
  }

  public ast.E.Sig visitSig(E.Sig s){
    return new ast.E.Sig(
      s.mdf(),
      s.gens().stream().map(this::visitGX).toList(),
      s.ts().stream().map(this::visitT).toList(),
      this.visitT(s.ret()),
      s.pos()
    );
  }

  public ast.Program visitProgram(astFull.Program p){
    Map<Id.DecId, T.Dec> coreDs = p.ds().entrySet().stream()
      .collect(Collectors.toMap(kv->kv.getKey(), kv->visitDec(kv.getValue())));
    return new ast.Program(coreDs);
  }

  private Id.GX<ast.T> visitGX(Id.GX<astFull.T> gx){
    return new Id.GX<>(gx.name());
  }
}
