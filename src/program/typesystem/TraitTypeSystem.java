package program.typesystem;

import ast.E.Meth;
import ast.T.Dec;
import ast.T;
import ast.Program;
import id.Id.IT;
import id.Mdf;
import failure.CompileError;
import failure.Res;
import failure.Fail;
import utils.Bug;
import utils.Mapper;

import java.util.List;
import java.util.Optional;

interface Gamma{
  default T get(ast.E.X x){ return getO(x).orElseThrow(); }
  default T get(String s){ return getO(s).orElseThrow(); }
  default Optional<T> getO(ast.E.X x){ return getO(x.name()); }
  Optional<T> getO(String s);
  static Gamma empty(){ return x->Optional.empty(); }
  default Gamma add(String s, T t){ return x->x.equals(s)?Optional.of(t):this.getO(x); }
}
public interface TraitTypeSystem {
  Program p();
  static List<CompileError> dsOk(List<Dec> ds){
    Program p = new Program(Mapper.of(c->ds.stream().forEach(e->c.put(e.name(),e))));
    TraitTypeSystem ttt=()->p;
    return ds.stream().flatMap(di->ttt.dOk(di).stream()).toList();
  }
  default Optional<CompileError> dOk(Dec d){
    var c=d.name();
    var xs=d.gxs();
    var b=d.lambda();
    assert b.selfName().equals("this");
    //TODO: is this ok, by reusing the other meth?
    //if so, should we remove the other meth from the formalism?
    var cT=new IT<T>(c,xs.stream().map(x->new T(Mdf.mdf,x)).toList());
    try{ var meths=p().meths(cT,0); }
    catch(CompileError ce){ return Optional.of(ce); }
    assert d.lambda().mdf()==Mdf.mdf;
    return bothT(Gamma.empty(),d).err();
  }
  default Res bothT(Gamma g,Dec d){ throw Bug.todo(); }

  default Optional<CompileError> mOk(Gamma g, T t, List<IT> its, Meth m){
    if(m.isAbs()){ return Optional.empty(); }
    var e=m.body().orElseThrow();
    Res res=e.accept(ETypeSystem.of(p(),g,t));
    var subOk=res.t()
      .flatMap(ti->!p().isSubType(ti,t)
        ? Optional.empty()
        : Optional.of(Fail.methTypeError(t, ti, m.name()).pos(m.pos()))
      );
    return res.err().or(()->subOk);
  }
}