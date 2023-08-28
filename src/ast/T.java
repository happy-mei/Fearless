package ast;

import failure.CompileError;
import failure.Fail;
import files.HasPos;
import files.Pos;
import id.Id;
import id.Id.DecId;
import id.Mdf;
import utils.Bug;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record T(Mdf mdf, Id.RT<T> rt) implements failure.Res{
  public <R> R resMatch(Function<T,R> ok, Function<CompileError,R> err){ return ok.apply(this); }
  @Override public String toString(){ return ""+mdf+" "+rt; }
  public T{
    assert mdf!=null && rt!=null;
    assert !(rt instanceof Id.IT<T> it) || it.ts().stream().flatMap(T::flatten).noneMatch(t->t.mdf().isIso()) : rt;
  }
  public <R> R match(Function<Id.GX<T>,R>gx, Function<Id.IT<T>,R>it){ return rt.match(gx, it); }
  public Id.IT<T> itOrThrow() { return this.match(gx->{ throw Bug.unreachable(); }, it->it); }
  public Id.GX<T> gxOrThrow() {
    return match(gx->gx, it->{ throw Bug.of("Expected GX, got IT"); });
  }
  public boolean isIt() { return this.match(gx->false, it->true); }
  public boolean isGX() { return this.match(gx->true, it->false); }
  public astFull.T toAstFullT() {
    return this.match(
      gx->new astFull.T(mdf(), new Id.GX<>(gx.name())),
      it->{
        var ts = it.ts().stream().map(T::toAstFullT).toList();
        return new astFull.T(mdf(), new Id.IT<>(it.name(), ts));
      });
  }
  public Stream<Id.GX<T>> deepGXs() {
    return rt().match(Stream::of, it->it.ts().stream().flatMap(T::deepGXs));
  }

  public Stream<T> flatten() {
    return this.match(gx->Stream.of(this), it->
      Stream.concat(Stream.of(this), it.ts().stream().flatMap(T::flatten))
    );
  }

  public T withMdf(Mdf mdf){ return new T(mdf,rt); }

  public record Dec(DecId name, List<Id.GX<T>> gxs, E.Lambda lambda, Optional<Pos> pos) implements HasPos {
    public Dec{ assert gxs.size()==name.gen() && lambda!=null; }
    public ast.T.Dec withName(Id.DecId name) { return new ast.T.Dec(name,gxs,lambda,pos); }
    public ast.T.Dec withLambda(ast.E.Lambda lambda) { return new ast.T.Dec(name,gxs,lambda,pos); }

    public Id.IT<T> toIT(){
      return new Id.IT<>(//AstFull.T || Ast.T
        this.name(),
        this.gxs().stream().map(gx->new T(Mdf.mdf, new Id.GX<>(gx.name()))).toList()
      );
    }
    @Override public String toString() {
      return "Dec[name="+name+",gxs=["+gxs.stream().map(Id.GX::toStringWithBounds).collect(Collectors.joining(","))+"],lambda="+lambda+"]";
    }
  }
}