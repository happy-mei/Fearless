package astFull;

import java.util.List;
import java.util.Optional;

import ast.Mdf;

public interface E {
  T t();
  record Lambda(Mdf mdf, List<T.IT>its, String selfName, List<Meth>meths, T t) implements E{}
  record MCall(E receiver,MethName name,Optional<List<T>>ts,List<E>es, T t)implements E{}
  record X(String name, T t) implements E{
    @Override public String toString(){ return name+":"+t; }
  }
  record MethName(String name){
    @Override public String toString(){ return name; }
  }
  record Meth(Optional<Sig> sig,Optional<MethName> name, List<E.X>xs,Optional<E> body){}
  record Sig(Mdf mdf, List<T.GX> gens, List<T> ts, T ret){};
}

