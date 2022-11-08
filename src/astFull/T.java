package astFull;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import ast.Mdf;

public record T(Mdf mdf, RT rt){
  public static final T infer = new T(null,null);
  public boolean isInfer(){ return this==infer; }
  
  interface RT{ <R> R match(Function<GX,R>gx,Function<IT,R>it,Function<GIT,R>git); }

  public record GX(String name)implements RT{
    public <R> R match(Function<GX,R>gx,Function<IT,R>it,Function<GIT,R>git){ return gx.apply(this); }
  }
  interface IT extends RT{}
  public record GIT(String name, List<T> ts)implements IT{
    public <R> R match(Function<GX,R>gx,Function<IT,R>it,Function<GIT,R>git){ return git.apply(this); }
  }
  public record Alias(T.IT from, String to){}
  public record Dec(String name, List<T.GX>xs,E.Lambda lambda){}
}