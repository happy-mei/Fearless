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
import program.TypeRename;
import utils.Box;
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
      return Fail.noCandidateMeths(e, expectedT().orElseThrow(), optTst.get().stream().distinct().toList()).pos(e.pos());
    }

    List<E> es = Push.of(e0,e.es());
    var nestedErrors = new ArrayDeque<ArrayList<CompileError>>(tst.size());
    for (TsT(List<T> ts, T t, boolean _hasRecMdf) : tst) {
      var errors = new ArrayList<CompileError>();
      nestedErrors.add(errors);
      if (okAll(es, ts, errors)) {
        return t;
      }
    }

    var calls1 = tst.stream()
      .map(tst1->{
        var call = Streams.zip(es, tst1.ts())
          .map((e1,t1)->{
            var getT = this.withT(Optional.empty());
            return e1.accept(getT).t()
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
    var calls= String.join("\n", calls1);
    return Fail.callTypeError(e, calls).pos(e.pos());
  }
  default boolean filterOnRes(TsT tst){
    if(expectedT().isEmpty()){ return true; }
    return p().isSubType(tst.t().mdf(), expectedT().get().mdf());
  }
  @Override default boolean okAll(List<E> es, List<T> ts, ArrayList<CompileError> errors) {
    return Streams.zip(es,ts).allMatch((e, t)->ok(e, t, errors));
  }
  default boolean ok(E e, T t, ArrayList<CompileError> errors) {
    var v = this.withT(Optional.of(t));
    var res = e.accept(v);
    if (res.t().isEmpty()){
      res.err().ifPresent(errors::add);
      return false;
    }
    return p().tryIsSubType(res.tOrThrow(), t);
  }

  default Optional<List<TsT>> multiMeth(T rec, MethName m, List<T> ts) {
    return resolveMeth(rec, m, ts).map(this::allMeth);
  }

  default List<TsT> allMeth(TsT tst) {
    TsT safeTsT = tst;
    if (tst.hasRecMdfRet) {
      safeTsT = new TsT(tst.ts, tst.t.withMdf(Mdf.recMdf), true);
    }
    return Stream.concat(Stream.of(
      tst,
      safeTsT.renameMdfs(Map.of(Mdf.mut, Mdf.iso)),
      safeTsT.renameMdfs(Map.of(
        Mdf.read, Mdf.imm,
        Mdf.lent, Mdf.iso,
        Mdf.mut, Mdf.iso
      ))),
      oneLentToMut(safeTsT).stream())
      .map(candidate->{
        if (!candidate.t.mdf().isRecMdf()) { return candidate; }
        if (tst.t.mdf().isRecMdf()) { return candidate; }
        var retT = fancyRename(candidate.t, candidate.ts.get(0).mdf(), Map.of());
        return new TsT(candidate.ts, retT, true);
      })
      .distinct()
      .toList();
  }

  default Optional<TsT> resolveMeth(T rec, MethName m, List<T> ts) {
    if (!(rec.rt() instanceof Id.IT<T> recIT)) { return Optional.empty(); }
    return p().meths(rec.mdf(), recIT, m, depth()).map(cm -> {
      var mdf = rec.mdf();
      var mdf0 = cm.mdf();
      Map<GX<T>,T> xsTsMap = Mapper.of(c->Streams.zip(cm.sig().gens(), ts).forEach(c::put));
      var t0 = rec.withMdf(mdf0);
      var params = Push.of(
        t0,
        cm.sig().ts().stream().map(ti->fancyRename(ti, mdf, xsTsMap)).toList()
      );
      var t = fancyRename(cm.ret(), mdf, xsTsMap);
      return new TsT(params, t, cm.ret().mdf().isRecMdf());
    });
  }

  /** This is [MDF, Xs=Ts] (recMdf rewriting for meth calls) */
  default T fancyRename(T t, Mdf mdf0, Map<GX<T>,T> map) {
    Mdf mdf=t.mdf();
    return t.match(
      gx->{
        if(!mdf.isRecMdf()){
          var renamed = map.getOrDefault(gx, t);
          return TypeRename.core(p()).propagateMdf(mdf, renamed);
        }
        var ti = map.get(gx);
        if (ti == null) { return t; }
//        var ti = map.getOrDefault(gx,t);
        // TODO: what about capturing a function from read to read?
        return ti.withMdf(mdf0.adapt(ti, Mdf.AdaptType.ResolveRecMdf));
      },
      it->{
        var newTs = it.ts().stream().map(ti->fancyRename(ti, mdf0, map)).toList();
        if(!mdf.isRecMdf() && !mdf.isMdf()){ return new T(mdf, it.withTs(newTs)); }
        if(mdf0.isIso()){ return new T(Mdf.mut, it.withTs(newTs)); }
        return new T(mdf0, it.withTs(newTs));
      });
  }
  default List<T> mutToIso(List<T> ts){
    return ts.stream().map(this::mutToIso).toList();
  }
  default T mutToIso(T t){ return t.mdf().isMut()?t.withMdf(Mdf.iso):t; }
  default T toLent(T t) { return t.mdf().isMut() ? t.withMdf(Mdf.lent) : t; }
  default TsT transformMuts(int i, List<T> ts, T t, boolean hasRecMdf){
    var ts0 = IntStream.range(0,ts.size()).mapToObj(j->j==i
      ? ts.get(i).withMdf(Mdf.lent)
      : mutToIso(ts.get(j))
    ).toList();
    return new TsT(ts0, mutToIso(t), hasRecMdf);
  }
  default List<TsT> oneLentToMut(TsT tst){
    var ts = tst.ts();
    var t = tst.t();
    Stream<TsT> r = Stream.of(new TsT(mutToIso(ts),toLent(t), tst.hasRecMdfRet));
    var muts = IntStream.range(0,ts.size())
      .filter(i->ts.get(i).mdf().isMut()).boxed().toList();
    Stream<TsT> ps=muts.stream()
      .map(i->transformMuts(i, ts, t, tst.hasRecMdfRet));
    return Stream.concat(r,ps).toList();
  }

  static boolean containsRecMdf(T t) {
    return t.mdf().isRecMdf() || t.match(
      gx->false,
      it->it.ts().stream().anyMatch(EMethTypeSystem::containsRecMdf)
    );
  }

  record TsT(List<T> ts, T t, boolean hasRecMdfRet){
    public TsT renameMdfs(Map<Mdf, Mdf> replacements) {
      List<T> ts = ts().stream().map(ti->ti.withMdf(replacements.getOrDefault(ti.mdf(), ti.mdf()))).toList();
      T t = t().withMdf(replacements.getOrDefault(t().mdf(), t().mdf()));
      return new TsT(ts, t, hasRecMdfRet);
    }
    @Override public String toString() {
      return "("+ts.stream().map(T::toString).collect(Collectors.joining(", "))+"): "+t;
    }
  }
}
