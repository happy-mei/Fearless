package ast;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import parser.Parser;


//TODO: discuss: may be MethName and DecId does not need duplication

public interface E {
  E accept(CloneVisitor v);
  <R> Optional<R> accept(CollectorVisitor<R> v);

  record Lambda(Mdf mdf, List<T.IT> its, String selfName, List<Meth> meths) implements E{
    public Lambda{
      assert mdf!=null;
      assert !its.isEmpty();
      assert X.validId(selfName);
      assert meths!=null;
    }
    @Override public E accept(CloneVisitor v){ return v.visitLambda(this); }
    @Override public <R>Optional<R> accept(CollectorVisitor<R> v){return v.visitLambda(this);}
  }
  record MCall(E receiver, MethName name, List<T> ts, List<E> es)implements E{
    @Override public E accept(CloneVisitor v){return v.visitMCall(this);}
    @Override public <R>Optional<R> accept(CollectorVisitor<R> v){return v.visitMCall(this);}
    @Override public String toString() {
      return String.format("%s %s%s(%s)", receiver, name, ts, es);
    }
  }
  record X(String name) implements E{
    public X{ assert validId(name); }
    
    public static boolean validId(String x){///TODO: same for m and pgk.C
      assert x!=null && !x.isEmpty();
      return new parser.Parser(Parser.dummy,x).parseX();      
    }
    @Override public E accept(CloneVisitor v){ return v.visitX(this); }
    @Override public <R>Optional<R> accept(CollectorVisitor<R> v){ return v.visitX(this); }
    @Override public String toString(){ return name; }
  }
  record MethName(String name){
    @Override public String toString(){ return name; }
  }
  record Meth(Sig sig, MethName name, List<String> xs, Optional<E> body){
    @Override public String toString() {
      return String.format("%s(%s): %s -> %s", name, xs, sig, body.map(Object::toString).orElse("[-]"));
    }
  }
  record Sig(Mdf mdf, List<T.GX> gens, List<T> ts, T ret){}
}

record T(Mdf mdf, RT rt){
  public <R> R match(Function<GX,R>gx,Function<IT,R>it){ return rt.match(gx, it); }
  public interface RT{ <R> R match(Function<GX,R> gx, Function<IT,R> it); }

  public record GX(String name)implements RT{
    public <R> R match(Function<GX,R>gx, Function<IT,R>it){ return gx.apply(this); }
  }
  public record IT(String name, List<T> ts)implements RT{
    public <R> R match(Function<GX,R>gx, Function<IT,R>it){ return it.apply(this); }
    public IT withTs(List<T>ts){ return new IT(name,ts); }
    @Override public String toString(){ return name+ts; }
  }
  public record DecId(String name,int gen){//repeated, it could have a different invariants (requires pkg.)
    @Override public String toString() {
      return String.format("%s/%d", name, gen);
    }
  }
  public record Dec(String name, List<T.GX> gxs, E.Lambda lambda){ }
  @Override public String toString(){ return ""+mdf+" "+rt; }
}

interface CloneVisitor{
  E visitLambda(E.Lambda e);
  E visitX(E.X e);
  E visitMCall(E.MCall e);
}
interface CollectorVisitor<R>{
  Optional<R> visitLambda(E.Lambda e);
  Optional<R> visitX(E.X e);
  Optional<R> visitMCall(E.MCall e);
}