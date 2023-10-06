package program.typesystem;

import ast.E;
import ast.Program;
import ast.T;
import ast.T.Dec;
import failure.CompileError;
import id.Id;
import id.Id.IT;
import id.Mdf;
import utils.Mapper;

import java.util.*;
import java.util.function.Supplier;

public interface TraitTypeSystem {
  Program p();
  static List<CompileError> dsOk(Collection<Dec> ds, IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls){
    Map<Id.DecId, Dec> pDs = Mapper.of(c->ds.forEach(e->c.put(e.name(),e)));
    TraitTypeSystem ttt = ()->new Program(pDs);
    return ds.stream().flatMap(di->ttt.dOk(di, resolvedCalls).stream()).toList();
  }
  default Optional<CompileError> dOk(Dec d, IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls){
    var c=d.name();
    var xs=d.gxs();
    var b=d.lambda();
    assert b.selfName().equals("this");
    var cT=new IT<>(c,xs.stream().map(x->new T(Mdf.mdf,x)).toList());
    var xbs = XBs.empty().addBounds(d.gxs(), d.bounds());
    try{ p().meths(xbs, Mdf.recMdf, cT,0); }
    catch(CompileError ce){ return Optional.of(ce); }
    assert d.lambda().mdf()==Mdf.mdf;
    return ETypeSystem.of(p(), Gamma.empty(), xbs, Optional.empty(), resolvedCalls, 0).bothT(d).err();
  }
}