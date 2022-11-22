package ast;

import id.Id;
import id.Id.DecId;
import id.Mdf;

import java.util.List;
import java.util.function.Function;

public record T(Mdf mdf, Id.RT<T> rt){
  @Override public String toString(){ return ""+mdf+" "+rt; }
  public T{ assert mdf!=null && rt!=null; }
  public <R> R match(Function<Id.GX<T>,R>gx, Function<Id.IT<T>,R>it){ return rt.match(gx, it); }

  public record Dec(DecId name, List<Id.GX<T>> gxs, E.Lambda lambda){
    public Dec{ assert gxs.size()==name.gen() && lambda!=null; }
  }
}