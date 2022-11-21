package astFull;

import ast.Mdf;
import visitors.FullCloneVisitor;
import visitors.FullCollectorVisitor;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public record T(Mdf mdf, RT rt){
  public static final T infer = new T(null,null);
  public boolean isInfer(){ return this==infer; }
  public <R> R match(Function<GX,R>gx,Function<IT,R>it){ return rt.match(gx, it); }
  public Stream<T> flatten() {
    return this.match(gx->Stream.of(this), it->
      Stream.concat(Stream.of(this), it.ts().stream().flatMap(T::flatten))
    );
  }
  public interface RT{ <R> R match(Function<GX,R>gx,Function<IT,R>it); }

  public record GX(String name)implements RT{
    public <R> R match(Function<GX,R>gx,Function<IT,R>it){ return gx.apply(this); }
  }
  public record IT(String name, List<T> ts)implements RT{
    public <R> R match(Function<GX,R>gx,Function<IT,R>it){ return it.apply(this); }
    public IT withTs(List<T>ts){ return new IT(name,ts); }
    @Override public String toString(){ return name+ts; }
  }
  public record DecId(String name,int gen){
    @Override public String toString() {
      return String.format("%s/%d", name, gen);
    }
  }
  public record Alias(T.IT from, String to){
    public Alias accept(FullCloneVisitor v) { return v.visitAlias(this); }
    public <R> Optional<R> accept(FullCollectorVisitor<R> v) { return v.visitAlias(this); }
    @Override public String toString() {
      return String.format("alias %s as %s", from, to);
    }
  }
  public record Dec(String name, List<T.GX> gxs, E.Lambda lambda){
    public Dec accept(FullCloneVisitor v) { return v.visitDec(this); }
    public <R> Optional<R> accept(FullCollectorVisitor<R> v) { return v.visitDec(this); }
  }
  @Override public String toString(){
    if(isInfer()){ return "infer"; }
    return ""+mdf+" "+rt;
  }
}