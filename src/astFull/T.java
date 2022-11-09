package astFull;

import java.util.List;
import java.util.function.Function;

import ast.Mdf;

public record T(Mdf mdf, RT rt){
  public static final T infer = new T(null,null);
  public boolean isInfer(){ return this==infer; }
  public <R> R match(Function<GX,R>gx,Function<IT,R>it){ return rt.match(gx, it); }
  interface RT{ <R> R match(Function<GX,R>gx,Function<IT,R>it); }

  public record GX(String name)implements RT{
    public <R> R match(Function<GX,R>gx,Function<IT,R>it){ return gx.apply(this); }
  }
  public record IT(String name, List<T> ts)implements RT{
    public <R> R match(Function<GX,R>gx,Function<IT,R>it){ return it.apply(this); }
    public IT withTs(List<T>ts){ return new IT(name,ts); }
    @Override public String toString(){ return name+ts; }
  }
  public record DecId(String name,int gen){}
  public record Alias(T.IT from, String to){}
  public record Dec(String name, List<T.GX>xs,E.Lambda lambda){}
  @Override public String toString(){
    if(isInfer()){ return "infer"; }
    return ""+mdf+" "+rt;
  }
}