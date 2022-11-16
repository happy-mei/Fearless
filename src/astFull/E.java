package astFull;

import ast.Mdf;

import java.util.List;
import java.util.Optional;

public interface E {
  T t();
  record Lambda(Mdf mdf, List<T.IT>its, String selfName, List<Meth>meths, T t) implements E{}
  record MCall(E receiver,MethName name,Optional<List<T>>ts,List<E>es, T t)implements E{}
  record X(String name, T t) implements E{
    public static final String RESERVED = "fearIntrinsic";
    private static int FRESH_N = 0;
    public static void reset() { FRESH_N = 0; }
    public X(T t){
      this(RESERVED + FRESH_N++, t);
    }
    @Override public String toString(){ return name+":"+t; }
  }
  record MethName(String name){
    @Override public String toString(){ return name; }
  }
  record Meth(Optional<Sig> sig,Optional<MethName> name, List<E.X>xs,Optional<E> body){
    @Override public String toString() {
      return String.format("%s(%s): %s -> %s", name.map(Object::toString).orElse("[-]"), xs, sig.map(Object::toString).orElse("[-]"), body.map(Object::toString).orElse("[-]"));
    }
  }
  record Sig(Mdf mdf, List<T.GX> gens, T ret){}
}

