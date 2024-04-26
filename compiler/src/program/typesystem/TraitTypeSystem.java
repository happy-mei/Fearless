package program.typesystem;

import ast.Program;
import ast.T;
import ast.T.Dec;
import failure.CompileError;
import id.Id;
import id.Id.IT;
import id.Mdf;
import program.TypeSystemFeatures;
import utils.Mapper;
import utils.Push;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public interface TraitTypeSystem {
  Program p();
  static List<Supplier<CompileError>> dsOk(TypeSystemFeatures tsf, Collection<Dec> ds, ConcurrentHashMap<Long, TsT> resolvedCalls){
    Map<Id.DecId, Dec> pDs = Mapper.of(c->ds.forEach(e->c.put(e.name(),e)));
    TraitTypeSystem ttt = ()->new Program(tsf, pDs, Map.of());
    return ds.stream()
      .flatMap(di->ttt.dOk(di, resolvedCalls).stream())
      .toList();
  }
  default Optional<Supplier<CompileError>> dOk(Dec d, ConcurrentHashMap<Long, TsT> resolvedCalls){
    var c=d.name();
    var xs=d.gxs();
    var b=d.lambda();
    assert b.selfName().equals("this");
    var cT=new IT<>(c,xs.stream().map(x->new T(Mdf.mdf,x)).toList());
    var xbs = XBs.empty().addBounds(d.gxs(), d.bounds());
    try{ p().meths(xbs, Mdf.recMdf, cT,0); }
    catch(CompileError ce){ return Optional.of(()->ce); }
    assert d.lambda().mdf()==Mdf.mdf: d.lambda().mdf();
    var self= (ELambdaTypeSystem) ETypeSystem.of(
      p(), Gamma.empty(), xbs, List.of(), resolvedCalls, 0);
    return self.bothT(d).asOpt();
  }
}