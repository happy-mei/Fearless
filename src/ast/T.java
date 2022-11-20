package ast;

import java.util.List;
import java.util.function.Function;

import id.Id;
import id.Id.DecId;

public record T(Mdf mdf, RT rt){
  @Override public String toString(){ return ""+mdf+" "+rt; }
  public T{ assert mdf!=null && rt!=null; }
  public <R> R match(Function<GX,R>gx,Function<IT,R>it){ return rt.match(gx, it); }
  public interface RT{ <R> R match(Function<GX,R> gx, Function<IT,R> it); }

  public record GX(String name)implements RT{
    public GX{assert Id.validGX(name);}
    public <R> R match(Function<GX,R>gx, Function<IT,R>it){ return gx.apply(this); }
  }
  public record IT(String name, List<T> ts)implements RT{
    public IT{ assert Id.validDecName(name) && ts!=null;}
    public <R> R match(Function<GX,R>gx, Function<IT,R>it){ return it.apply(this); }
    public IT withTs(List<T>ts){ return new IT(name,ts); }
    @Override public String toString(){ return name+ts; }
  }
  public record Dec(DecId name, List<T.GX> gxs, E.Lambda lambda){
    public Dec{ assert gxs.size()==name.gen() && lambda!=null; }
  }
}