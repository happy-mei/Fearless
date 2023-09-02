package program.typesystem;

import ast.Program;
import ast.T;
import ast.T.Dec;
import failure.CompileError;
import id.Id.IT;
import id.Mdf;
import utils.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TraitTypeSystem {
  Program p();
  static List<CompileError> dsOk(Collection<Dec> ds){
    Program p = new Program(Mapper.of(c->ds.forEach(e->c.put(e.name(),e))));
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
    var cT=new IT<>(c,xs.stream().map(x->new T(Mdf.mdf,x)).toList());
    try{ p().meths(Mdf.recMdf, cT,0); }
    catch(CompileError ce){ return Optional.of(ce); }
    assert d.lambda().mdf()==Mdf.mdf;
    // TODO: get XBs
    return ETypeSystem.of(p(),Gamma.empty(), XBs.empty(), Optional.empty(),0).bothT(d).err();
  }
}