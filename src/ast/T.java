package ast;

import id.Id;
import id.Id.DecId;
import id.Mdf;
import utils.Bug;

import java.util.List;
import java.util.function.Function;

public record T(Mdf mdf, Id.RT<T> rt){
  @Override public String toString(){ return ""+mdf+" "+rt; }
  public T{ assert mdf!=null && rt!=null; }
  public <R> R match(Function<Id.GX<T>,R>gx, Function<Id.IT<T>,R>it){ return rt.match(gx, it); }
  public Id.IT<T> itOrThrow() { return this.match(gx->{ throw Bug.unreachable(); }, it->it); }
  public boolean isIt() { return this.match(gx->false, it->true); }
  public astFull.T toAstFullT() {
    return this.match(
      gx->new astFull.T(mdf(), new Id.GX<>(gx.name())),
      it->{
        var ts = it.ts().stream().map(T::toAstFullT).toList();
        return new astFull.T(mdf(), new Id.IT<>(it.name(), ts));
      });
  }

  public T withMdf(Mdf mdf){ return new T(mdf,rt); }

  public record Dec(DecId name, List<Id.GX<T>> gxs, E.Lambda lambda){
    public Dec{ assert gxs.size()==name.gen() && lambda!=null; }
  }
}