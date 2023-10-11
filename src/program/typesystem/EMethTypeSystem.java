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
import program.CM;
import program.TypeRename;
import utils.Bug;
import utils.Mapper;
import utils.Push;
import utils.Streams;
import visitors.Visitor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface EMethTypeSystem extends ETypeSystem {
  // TODO: we have to be more permissive first (i.e. mut before read/imm) because e0.accept(v) will work until the final step and then fail, at which point we cannot backtrack.
  List<Mdf> inferPriority = List.of(Mdf.iso, Mdf.mut, Mdf.imm, Mdf.recMdf, Mdf.read, Mdf.lent, Mdf.readOnly);
  List<Mdf> recvPriority = inferPriority;
//  List<Mdf> recvPriority = List.of(Mdf.readOnly, Mdf.imm, Mdf.read, Mdf.recMdf, Mdf.iso, Mdf.lent, Mdf.mut);
//  List<Mdf> inferPriority = recvPriority;

  default Res visitMCall(E.MCall e) {
    var e0 = e.receiver();

    var guessType = new Visitor<Set<T>>(){
      public Set<T> guessRecvType(E.MCall e) {
        Stream<CM> cms;
        if (e.receiver() instanceof E.Lambda l) {
          var tmpDec = T.Dec.ofComposite(l.its());
          cms = p().withDec(tmpDec).meths(xbs(), Mdf.recMdf, tmpDec.toIT(), e.name(), depth()).stream();
        } else {
          var recv = e.receiver().accept(this);
          cms = recv.stream()
            .flatMap(recvT->p().meths(xbs(), Mdf.recMdf, recvT.itOrThrow(), e.name(), depth()).stream());
        }
        return cms.map(cm->new T(Mdf.recMdf, cm.c())).collect(Collectors.toSet());
      }
      @Override public Set<T> visitMCall(E.MCall e) {
        Stream<CM> cms;
        if (e.receiver() instanceof E.Lambda l) {
          var tmpDec = T.Dec.ofComposite(l.its());
          cms = p().withDec(tmpDec).meths(xbs(), Mdf.recMdf, tmpDec.toIT(), e.name(), depth()).stream();
        } else {
          var recv = e.receiver().accept(this);
          cms = recv.stream()
            .flatMap(recvT->p().meths(xbs(), Mdf.recMdf, recvT.itOrThrow(), e.name(), depth()).stream());
        }
        var renamer = TypeRename.core(p());
        return cms.map(cm->{
          var sig = renamer.renameSigOnMCall(cm.sig(), xbs().addBounds(cm.sig().gens(), cm.bounds()), renamer.renameFun(e.ts(), cm.sig().gens()));
          return sig.ret().withMdf(Mdf.recMdf);
        }).collect(Collectors.toSet());
      }

      @Override public Set<T> visitX(E.X e) {
        return Set.of(g().get(e).withMdf(Mdf.recMdf));
      }

      @Override public Set<T> visitLambda(E.Lambda e) {
        throw Bug.unreachable();
      }
    };

    var test = guessType.guessRecvType(e)
      .stream()
      .map(expected->visitMCall(e, expected))
      .toList();

//    Res expectedRecv = this.guessType().guessRecvType(e);
    Res expectedRecv = new CompileError();
//    expectedRecv.err().ifPresent(System.err::println);
    var v = this.withT(Optional.empty());
    Res rE0 = e0.accept(v);
    if(rE0.err().isPresent()){ return rE0; }
    T t_=rE0.tOrThrow();

    return visitMCall(e, t_);
  }
  default Res visitMCall(E.MCall e, T recvT) {
    var optTst=multiMeth(recvT,e.name(),e.ts());
    if (optTst.isEmpty()) {
      throw Fail.undefinedMethod(e.name(), recvT, p().meths(xbs(), recvT.mdf(), recvT.itOrThrow(), depth()).stream());
//      return new CompileError();
    }
    List<TsT> tsts = optTst.stream()
      .filter(this::filterOnRes)
      .toList();

    if (tsts.isEmpty()) {
      return Fail.noCandidateMeths(e, expectedT().orElseThrow(), optTst.stream().distinct().toList()).pos(e.pos());
    }

    List<E> es = Push.of(e.receiver(),e.es());
    var nestedErrors = new ArrayDeque<ArrayList<CompileError>>(tsts.size());
    var methArgCache = IntStream.range(0, es.size()).mapToObj(i_->new HashMap<T, Res>()).toList();
    for (var tst : tsts) {
      var errors = new ArrayList<CompileError>();
      nestedErrors.add(errors);
      if (okAll(es, tst.ts(), errors, methArgCache)) {
        var invalidBounds = GenericBounds.validGenericMeth(p(), xbs(), recvT.mdf(), recvT.itOrThrow(), depth(), tst.original(), e.ts());
        if (invalidBounds.isPresent()) { return invalidBounds.get().pos(e.pos()); }
        resolvedCalls().put(e, tst);
        return tst.t();
      }
    }

    var calls1 = tsts.stream()
      .map(tst1->{
        var call = CompletableFuture.supplyAsync(()->Streams.zip(es, tst1.ts())
          .map((e1,t1)->e1.accept(this.withT(Optional.empty())).t()
            .map(T::toString)
            .orElse("?"+e1+"?"))
          .collect(Collectors.joining(", ")))
          .completeOnTimeout("<timed out>", 100, TimeUnit.MILLISECONDS)
          .join();
        var dependentErrorMsgs = nestedErrors.removeFirst().stream()
          .map(CompileError::toString)
          .collect(Collectors.joining("\n"))
          .indent(4);
        var dependentErrors = !dependentErrorMsgs.isEmpty()
          ? "\n"+"The following errors were found when checking this sub-typing:\n".indent(2)+dependentErrorMsgs
          : "";
        return "("+ String.join(", ", call) +") <: "+tst1+dependentErrors;
      }).toList();
    var calls = String.join("\n", calls1);
    return Fail.callTypeError(e, expectedT(), calls).pos(e.pos());
  }
  default boolean filterOnRes(TsT tst){
    if(expectedT().isEmpty()){ return true; }
    return p().isSubType(xbs(), tst.t(), expectedT().get());
  }
  default boolean okAll(List<E> es, List<T> ts, ArrayList<CompileError> errors, List<HashMap<T, Res>> caches) {
    assert es.size() == ts.size() && caches.size() == es.size();
    return IntStream.range(0, es.size())
      .allMatch(i->ok(es.get(i), ts.get(i), errors, caches.get(i)));
  }
  default boolean ok(E e, T t, ArrayList<CompileError> errors, HashMap<T, Res> cache) {
    var res = cache.computeIfAbsent(t, t_->e.accept(this.withT(Optional.of(t_))));
    // TODO: cache res based on the same visitor hashcode
    /*
     v has program (will stay the same)
     v has the T
     v has a gamma (should stay the same)
     v has a XBs (should stay the same)
     v has a depth (should stay the same)
     */
    if (res.t().isEmpty()){
      res.err().ifPresent(errors::add);
      return false;
    }
    return p().tryIsSubType(xbs(), res.tOrThrow(), t);
  }

  default List<TsT> multiMeth(T rec, MethName m, List<T> ts) {
    // TODO: throw error (no invoking methods on GXs)
    if (!(rec.rt() instanceof Id.IT<T> recIT)) { return List.of(); }

    return p().meths(xbs(), rec.mdf(), recIT, depth()).stream()
      .filter(cm->cm.name().nameArityEq(m))
      .sorted(Comparator.comparingInt(cm->recvPriority.indexOf(cm.mdf())))
      .map(cm->{
        var mdf = rec.mdf();
        Map<GX<T>,T> xsTsMap = Mapper.of(c->Streams.zip(cm.sig().gens(), ts).forEach(c::put));
        var xbs = xbs().addBounds(cm.sig().gens(), cm.sig().bounds());

        var params = Push.of(
          fancyRename(rec.rt().toString(), rec.withMdf(cm.mdf()), mdf, xsTsMap, TypeRename.RenameKind.Arg, xbs),
          Streams.zip(cm.xs(), cm.sig().ts())
            .map((xi, ti)->fancyRename(xi+": "+ti.rt().toString(), ti, mdf, xsTsMap, TypeRename.RenameKind.Arg, xbs))
            .toList()
        );
        var t = fancyRename(cm.ret().rt().toString(), cm.ret(), mdf, xsTsMap, TypeRename.RenameKind.Return, xbs);

        return new TsT(params, t, cm);
      })
      .flatMap(this::allMeth)
      .toList();
  }

  default Stream<TsT> allMeth(TsT tst) {
    return Stream.concat(Stream.of(
      tst,
      tst.renameMdfs(Map.of(
        Mdf.mut, Mdf.iso,
        Mdf.read, Mdf.imm
      )),
      tst.renameMdfs(Map.of(
        Mdf.readOnly, Mdf.imm,
        Mdf.read, Mdf.imm,
        Mdf.lent, Mdf.iso,
        Mdf.mut, Mdf.iso
      )),
      tst.renameTsMdfs(Map.of(
        Mdf.read, Mdf.readOnly,
        Mdf.lent, Mdf.iso,
        Mdf.mut, Mdf.iso
      )).renameTMdfs(Map.of(Mdf.mut,Mdf.lent,    Mdf.read,Mdf.readOnly))),
      oneLentToMut(tst).stream())
      .distinct();
  }

  /** This is [MDF, Xs=Ts] (recMdf rewriting for meth calls) */
  default T fancyRename(String x, T t, Mdf mdf0, Map<GX<T>,T> map, TypeRename.RenameKind kind, XBs xbs) {
    assert !mdf0.isMdf();
    Mdf mdf=t.mdf();
    return t.match(
      gx->{
        if(!mdf.isRecMdf()){
          var renamed = map.getOrDefault(gx, t);
          return TypeRename.core(p()).propagateMdf(mdf, renamed);
        }
        var ti = map.get(gx);
        if (ti == null) { return t; }

        return switch (kind) {
          case Return -> ti.withMdf(mdf0.adapt(ti));
          case Arg -> {
//            if (ti.mdf().isMdf()) { yield ti.withMdf(mdf0.adapt(ti)); } // this is probably unsound
            var newMdf = Gamma.xT(x, xbs, mdf0, ti, mdf0).mdf();
            yield ti.withMdf(newMdf);
          }
        };
      },
      it->{
        var newTs = it.ts().stream().map(ti->fancyRename(x, ti, mdf0, map, TypeRename.RenameKind.Arg, xbs)).toList();
        if(!mdf.isRecMdf() && !mdf.isMdf()){ return new T(mdf, it.withTs(newTs)); }
        if(mdf0.isIso()) { return new T(Mdf.mut, it.withTs(newTs)); }
        return new T(mdf0, it.withTs(newTs));
      });
  }
  default List<T> mutToIso(List<T> ts){
    return ts.stream().map(this::mutToIso).toList();
  }
  default T mutToIso(T t){ return t.mdf().isMut()?t.withMdf(Mdf.iso):t; }
  default T mutToLent(T t) { return t.mdf().isMut() ? t.withMdf(Mdf.lent) : t; }
  default TsT transformMuts(int i, List<T> ts, T t, CM original){
    var ts0 = IntStream.range(0,ts.size()).mapToObj(j->j==i
      ? ts.get(i).withMdf(Mdf.lent)
      : mutToIso(ts.get(j))
    ).toList();
    return new TsT(ts0, mutToLent(t), original);
  }
  default List<TsT> oneLentToMut(TsT tst){
    var ts = tst.ts();
    var t = tst.t();
    Stream<TsT> r = Stream.of(tst.with(mutToIso(ts), mutToLent(t)));
    var muts = IntStream.range(0,ts.size())
      .filter(i->ts.get(i).mdf().isMut()).boxed().toList();
    Stream<TsT> ps=muts.stream()
      .map(i->transformMuts(i, ts, t, tst.original));
    return Stream.concat(r,ps).toList();
  }

  static boolean containsRecMdf(T t) {
    return t.mdf().isRecMdf() || t.match(
      gx->false,
      it->it.ts().stream().anyMatch(EMethTypeSystem::containsRecMdf)
    );
  }

  record TsT(List<T> ts, T t, CM original){
    public TsT with(List<T> ts, T t) {
      return new TsT(ts, t, original);
    }
    public TsT renameMdfs(Map<Mdf, Mdf> replacements) {
      var ts = renameTsMdfs(replacements).ts();
      var t = renameTMdfs(replacements).t();
      return with(ts, t);
    }
    public TsT renameTsMdfs(Map<Mdf, Mdf> replacements) {
      List<T> ts = ts().stream().map(ti->ti.withMdf(replacements.getOrDefault(ti.mdf(), ti.mdf()))).toList();
      return with(ts, t);
    }
    public TsT renameTMdfs(Map<Mdf, Mdf> replacements) {
      T t = t().withMdf(replacements.getOrDefault(t().mdf(), t().mdf()));
      return with(ts, t);
    }
    @Override public String toString() {
      return "("+ts.stream().map(T::toString).collect(Collectors.joining(", "))+"): "+t;
    }
  }
}
