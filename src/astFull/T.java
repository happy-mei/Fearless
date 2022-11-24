package astFull;

import id.Id;
import id.Mdf;
import visitors.FullCloneVisitor;
import visitors.FullCollectorVisitor;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public record T(Mdf mdf, Id.RT<T> rt){
  public static final T infer = new T(null,null);
  public boolean isInfer(){ return this==infer; }
  public <R> R match(Function<Id.GX<T>,R>gx,Function<Id.IT<T>,R>it){
    assert !this.isInfer():"Can not match on infer";
    return rt.match(gx, it);
  }
  public Stream<T> flatten() {
    if (this.isInfer()) { return Stream.of(this); }
    return this.match(gx->Stream.of(this), it->
      Stream.concat(Stream.of(this), it.ts().stream().flatMap(T::flatten))
    );
  }
  public record Alias(Id.IT<T> from, String to){
    public Alias accept(FullCloneVisitor v) { return v.visitAlias(this); }
    public <R> Optional<R> accept(FullCollectorVisitor<R> v) { return v.visitAlias(this); }
    @Override public String toString() {
      return String.format("alias %s as %s", from, to);
    }
  }
  public record Dec(Id.DecId name, List<Id.GX<T>> gxs, E.Lambda lambda){
    public Dec{ assert name.gen()==gxs.size(); }
    public Dec accept(FullCloneVisitor v) { return v.visitDec(this); }
    public <R> Optional<R> accept(FullCollectorVisitor<R> v) { return v.visitDec(this); }
    public Dec withLambda(E.Lambda lambda) { return new Dec(name,gxs,lambda); }
  }
  @Override public String toString(){
    if(isInfer()){ return "infer"; }
    return ""+mdf+" "+rt;
  }
}