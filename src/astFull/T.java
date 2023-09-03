package astFull;

import files.HasPos;
import files.Pos;
import id.Id;
import id.Mdf;
import utils.Box;
import utils.Bug;
import visitors.FullCloneVisitor;
import visitors.FullShortCircuitVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class T implements Id.Ty {
  public static final T infer = new T();
  private T(){mdf=null;rt=null;}
  private final Mdf mdf;
  private final Id.RT<T> rt;
  public T(Mdf mdf, Id.RT<T> rt) {
    Objects.requireNonNull(mdf);
    Objects.requireNonNull(rt);
//    assert rt instanceof Id.GX<T> || mdf != Mdf.mdf;
    this.mdf = mdf;
    this.rt = rt;
  }
  public Mdf mdf() {
    assert !this.isInfer();
    return mdf;
  }
  public Id.RT<T> rt() {
    assert !this.isInfer();
    return rt;
  }
  public boolean isInfer() {
    return this == infer;
  }
  public ast.T toAstT() {
    return this.match(
      gx->new ast.T(mdf(), new Id.GX<>(gx.name())),
      it->{
        var ts = it.ts().stream().map(T::toAstT).toList();
        return new ast.T(mdf(), new Id.IT<>(it.name(), ts));
      });
  }
  public ast.T toAstTFreshenInfers(Box<Integer> nFresh) {
    if (this.isInfer()) {
      int n = nFresh.get();
      nFresh.set(n+1);
      return new ast.T(Mdf.mdf, new Id.GX<>("FearTmp"+n+"$"));
    }
    return this.match(
      gx->new ast.T(mdf(), new Id.GX<>(gx.name())),
      it->{
        var ts = it.ts().stream().map(t->t.toAstTFreshenInfers(nFresh)).toList();
        return new ast.T(mdf(), new Id.IT<>(it.name(), ts));
      });
  }
  /**Note: this is not considering Mdf.mdf specially.
   We discussed it and it important for the withers to be consistent.
   There are places where we purposely do .withMdf(Mdf.mdf)*/
  public T withMdf(Mdf mdf) {
    assert !this.isInfer();
    return new T(mdf, rt);
  }
  public T propagateMdf(Mdf mdf){
    assert !this.isInfer();
    if(mdf.isMdf()){ return this; }
    return this.withMdf(mdf);
  }
  public Id.IT<T> itOrThrow() {
    return match(gx->{
      throw Bug.of("Expected IT, got GX");
    }, it->it);
  }
  public Id.GX<T> gxOrThrow() {
    return match(gx->gx, it->{
      throw Bug.of("Expected GX, got IT");
    });
  }
  public <R> R match(Function<Id.GX<T>, R> gx, Function<Id.IT<T>, R> it) {
    if (this.isInfer()) { throw new MatchOnInfer(); }
    return rt.match(gx, it);
  }
  public Stream<T> flatten() {
    if (this.isInfer()) { return Stream.of(this); }
    return this.match(gx->Stream.of(this), it->
      Stream.concat(Stream.of(this), it.ts().stream().flatMap(T::flatten))
    );
  }

  public record Alias(Id.IT<T> from, String to, Optional<Pos> pos) implements HasPos {
    public Alias accept(FullCloneVisitor v) {
      return v.visitAlias(this);
    }

    public <R> Optional<R> accept(FullShortCircuitVisitor<R> v) {
      return v.visitAlias(this);
    }

    @Override
    public String toString() {
      return String.format("alias %s as %s", from, to);
    }
  }

  public record Dec(Id.DecId name, List<Id.GX<T>> gxs, Map<Id.GX<T>, Set<Mdf>> bounds, E.Lambda lambda, Optional<Pos> pos) implements HasPos {
    public Dec {
      assert name.gen() == gxs.size();
    }

    public Dec accept(FullCloneVisitor v) {
      return v.visitDec(this);
    }

    public <R> Optional<R> accept(FullShortCircuitVisitor<R> v) {
      return v.visitDec(this);
    }

    public T.Dec withName(Id.DecId name) { return new T.Dec(name, gxs, bounds, lambda, pos); }
    public Dec withLambda(E.Lambda lambda) {
      return new Dec(name, gxs, bounds, lambda, pos);
    }

    public Id.IT<ast.T> toAstT() {
      return new Id.IT<>(//AstFull.T || Ast.T
        this.name(),
        this.gxs().stream().map(gx->new ast.T(Mdf.mdf, new Id.GX<>(gx.name()))).toList()
      );
    }

    public Id.IT<T> toIT() {
      return new Id.IT<>(//AstFull.T || Ast.T
        this.name(),
        this.gxs().stream().map(gx->new T(Mdf.mdf, new Id.GX<>(gx.name()))).toList()
      );
    }

    @Override
    public String toString() {
      if (bounds.values().stream().mapToLong(Collection::size).sum() == 0) {
        return "Dec[name=" + name + ",gxs=[" + gxs.stream().map(Id.GX::toString).collect(Collectors.joining(",")) + "],lambda=" + lambda + "]";
      }
      return "Dec[name=" + name + ",gxs=[" + gxs.stream().map(Id.GX::toString).collect(Collectors.joining(",")) + "],bounds="+bounds+",lambda=" + lambda + "]";
    }
  }

  @Override
  public String toString() {
    if (isInfer()) {
      return "infer";
    }
    return "" + mdf + " " + rt;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (T) obj;
    return Objects.equals(this.mdf, that.mdf) &&
      Objects.equals(this.rt, that.rt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mdf, rt);
  }

  public static class MatchOnInfer extends RuntimeException{
    public MatchOnInfer() { super("Cannot match on infer."); }
  }
}