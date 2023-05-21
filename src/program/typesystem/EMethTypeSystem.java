package program.typesystem;

import ast.E;
import ast.T;
import failure.CompileError;
import failure.Fail;
import failure.Res;
import id.Id;
import id.Id.GX;
import id.Id.MethName;
import id.Mdf;
import utils.Mapper;
import utils.Push;
import utils.Streams;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface EMethTypeSystem extends ETypeSystem {
  default Res visitMCall(E.MCall e) {
    var e0 = e.receiver();
    var v = this.withT(Optional.empty());
    Res rE0 = e0.accept(v);
    if(rE0.err().isPresent()){ return rE0; }
    T t_=rE0.tOrThrow();
    var optTst=multiMeth(t_,e.name(),e.ts());
    if(optTst.isEmpty()){ return new CompileError(); } //TODO: list the available methods
    List<TsT> tst = optTst.get().stream()
      .filter(this::filterOnRes)
      .toList();

    if (tst.isEmpty()) {
      return Fail.noCandidateMeths(e, expectedT().orElseThrow(), optTst.get()).pos(e.pos());
    }

    List<E> es = Push.of(e0,e.es());
    var nestedErrors = new ArrayDeque<ArrayList<CompileError>>(tst.size());
    for (TsT(List<T> ts, T t) : tst) {
      var errors = new ArrayList<CompileError>();
      nestedErrors.add(errors);
      if (okAll(es, ts, null, errors)) {
        return t;
      }
    }

    var calls1 = tst.stream()
      .map(tst1->{
        var call = Streams.zip(es, tst1.ts())
          .map((e1,t1)->{
            var getT = this.withT(Optional.empty());
//            return e1.accept(getT).t().map(e1T->e1+": "+e1T);
            return e1.accept(getT).t()
//              .map(t->fancyRename(t, tst1.ts.get(0).mdf(), Map.of()))
              .map(T::toString)
              .orElseGet(()->"?"+e1+"?");
          })
          .toList();
        var dependentErrorMsgs = nestedErrors.removeFirst().stream()
          .map(CompileError::toString)
          .collect(Collectors.joining("\n"))
          .indent(4);
        var dependentErrors = dependentErrorMsgs.length() > 0
          ? "\n"+"The following errors were found when checking this sub-typing:\n".indent(2)+dependentErrorMsgs
          : "";
        return "("+ String.join(", ", call) +") <: "+tst1+dependentErrors;
      }).toList();
      var calls=calls1.stream().collect(Collectors.joining("\n"));
    return Fail.callTypeError(e, calls).pos(e.pos());
  }
  default boolean filterOnRes(TsT tst){
    if(expectedT().isEmpty()){ return true; }
    return p().isSubType(tst.t().mdf(), expectedT().get().mdf());
  }
  @Override default boolean okAll(List<E> es, List<T> ts, Mdf receiverMdf, ArrayList<CompileError> errors) {
    // TODO: this won't work because we need the receiverMdf for the recv in the lambda
    return Streams.zip(es,ts).allMatch((e, t)->ok(e, t, ts.get(0).mdf(), errors));
  }
  default boolean ok(E e, T t, Mdf recieverMdf, ArrayList<CompileError> errors) {
    var v = this.withT(Optional.of(t));
    var res = e.accept(v);
    if (res.t().isEmpty()){
      res.err().ifPresent(errors::add);
      return false;
    }
    var exprT = res.tOrThrow();
    return p().tryIsSubType(exprT, t);
  }

  default Optional<List<TsT>> multiMeth(T rec, MethName m, List<T> ts) {
    return extractMeth(rec, m, ts).map(this::allMeth);
  }

  default List<TsT> allMeth(TsT tst) {
    return Stream.concat(Stream.of(
      tst,
      tst.renameMdfs(Map.of(Mdf.mut, Mdf.iso)),
      tst.renameMdfs(Map.of(
        Mdf.read, Mdf.imm,
        Mdf.lent, Mdf.iso,
        Mdf.mut, Mdf.iso
      ))),
      oneLentToMut(tst).stream()
    ).toList();
  }

  default Optional<TsT> extractMeth(T rec, MethName m, List<T> ts) {
    if (!(rec.rt() instanceof Id.IT<T> recIT)) { return Optional.empty(); }
    return p().meths(recIT, m, depth()).map(cm -> {
      var mdf = rec.mdf();
      var mdf0 = cm.mdf();
      // TODO: this check is not in the formalism and is also kinda broken (see shouldApplyRecMdfInTypeParams4a)
      if (containsRecMdf(rec)) {
        return new TsT(Push.of(rec.withMdf(mdf0), cm.sig().ts()), cm.ret());
      }
      Map<GX<T>,T> xsTsMap = Mapper.of(c->Streams.zip(cm.sig().gens(), ts).forEach(c::put));
      var t0 = fancyRename(rec, mdf, xsTsMap).withMdf(mdf0);
//      var t0 = rec.withMdf(mdf0);
      var params = Push.of(
        t0,
        cm.sig().ts().stream().map(ti->fancyRename(ti, mdf, xsTsMap)).toList()
      );
      var t = fancyRename(cm.ret(), mdf, xsTsMap);
      return new TsT(params, t);
    });
  }

  /** This is [MDF, Xs=Ts] (recMdf rewriting for meth calls) */
  static T fancyRename(T t, Mdf mdf0, Map<GX<T>,T> map) {
    Mdf mdf=t.mdf();
    var renamed = t.match(
      gx->{
        if(!mdf.isRecMdf()){ return map.getOrDefault(gx,t); }
        var ti = map.getOrDefault(gx,t);
        return ti.withMdf(mdf0.adapt(ti));
      },
      it->{
        var newTs = it.ts().stream().map(ti->fancyRename(ti, mdf0, map)).toList();
        if(!mdf.isRecMdf() && !mdf.isMdf()){ return new T(mdf, it.withTs(newTs)); }
        if(mdf0.isIso()){ return new T(Mdf.mut, it.withTs(newTs)); }
        return new T(mdf0, it.withTs(newTs));
      });
//    assert !renamed.mdf().isRecMdf() : "recMdf should be flattened by now";
//    System.out.println(renamed);
    return renamed;
//    return t; // todo: disabling for now
  }
  default List<T> mutToIso(List<T> ts){
    return ts.stream().map(this::mutToIso).toList();
  }
  default T mutToIso(T t){ return t.mdf().isMut()?t.withMdf(Mdf.iso):t; }
  default TsT transformLents(int i,List<T> ts, T t){
    var ts0 = IntStream.range(0,ts.size()).mapToObj(j->j==i
      ? ts.get(i).withMdf(Mdf.mut)
      : mutToIso(ts.get(j))
    ).toList();
    return new TsT(ts0,mutToIso(t));
  }
  default List<TsT> oneLentToMut(TsT tst){
    Stream<TsT> r = Stream.of();
    var ts = tst.ts();
    var t = tst.t();
    if(t.mdf().isMut()){ r=Stream.of(new TsT(mutToIso(ts),t.withMdf(Mdf.mut))); }
    var lents = IntStream.range(0,ts.size())
      .filter(i->ts.get(i).mdf().isLent()).boxed().toList();
    Stream<TsT> ps=lents.stream()
      .map(i->transformLents(i,ts,t));
    return Stream.concat(r,ps).toList();
  }

  static boolean containsRecMdf(T t) {
    return t.mdf().isRecMdf() || t.match(
      gx->false,
      it->it.ts().stream().anyMatch(EMethTypeSystem::containsRecMdf)
    );
  }

  record TsT(List<T> ts, T t){
    public TsT renameMdfs(Map<Mdf, Mdf> replacements) {
      List<T> ts = ts().stream().map(ti->ti.withMdf(replacements.getOrDefault(ti.mdf(), ti.mdf()))).toList();
      T t = t().withMdf(replacements.getOrDefault(t().mdf(), t().mdf()));
      return new TsT(ts, t);
    }
    @Override public String toString() {
      return "("+ts.stream().map(T::toString).collect(Collectors.joining(", "))+"): "+t;
    }
  }
}
