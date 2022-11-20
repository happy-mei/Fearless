package ast;

import java.util.List;
import java.util.function.Function;

import parser.Parser;

public record T(Mdf mdf, RT rt){
  @Override public String toString(){ return ""+mdf+" "+rt; }
  public T{ assert mdf!=null && rt!=null; }
  public <R> R match(Function<GX,R>gx,Function<IT,R>it){ return rt.match(gx, it); }
  public interface RT{ <R> R match(Function<GX,R> gx, Function<IT,R> it); }

  public record GX(String name)implements RT{
    public GX{assert DecId.validGX(name);}
    public <R> R match(Function<GX,R>gx, Function<IT,R>it){ return gx.apply(this); }
  }
  public record IT(String name, List<T> ts)implements RT{
    public IT{ assert DecId.validDecName(name) && ts!=null;}
    public <R> R match(Function<GX,R>gx, Function<IT,R>it){ return it.apply(this); }
    public IT withTs(List<T>ts){ return new IT(name,ts); }
    @Override public String toString(){ return name+ts; }
  }
  public record DecId(String name,int gen){//repeated, it could have a different invariants (requires pkg.)
    public static boolean validDecName(String name){
      assert name!=null && !name.isEmpty();
      return new parser.Parser(Parser.dummy,name).parseFullCN();      
    }
    public static boolean validGX(String name){//TODO: here in case we merge DecIDs in a single 'IDs' place 
      assert name!=null && !name.isEmpty();
      return new parser.Parser(Parser.dummy,name).parseGX();      
    }
    public DecId{ assert validDecName(name) && gen>=0; }
    @Override public String toString() {
      return String.format("%s/%d", name, gen);
    }
  }
  public record Dec(DecId name, List<T.GX> gxs, E.Lambda lambda){
    public Dec{ assert gxs.size()==name.gen() && lambda!=null; }
  }
}