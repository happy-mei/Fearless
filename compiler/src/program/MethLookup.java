package program;

import ast.T;
import failure.Fail;
import files.Pos;
import id.Id;
import id.Mdf;
import id.Normaliser;
import id.Refresher;
import program.typesystem.SubTyping;
import program.typesystem.XBs;
import utils.Push;
import utils.Streams;
import visitors.CloneVisitor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MethLookup extends TypeTable, SubTyping {
  List<NormResult> cMsOf(Mdf recvMdf, Id.IT<T> t);

  default List<CM> meths(XBs xbs, Mdf recvMdf, Id.IT<T> it, Id.MethName name, int depth){
    return meths(xbs, recvMdf, it, depth).stream().filter(mi->mi.name().nameArityEq(name)).toList();
  }

  default List<CM> meths(XBs xbs, Mdf recvMdf, ast.E.Lambda l, int depth) {
    return methsAux(xbs, recvMdf, l.id().toIT()).stream().map(cm->freshenMethGens(cm, depth)).toList();
  }

  default List<CM> meths(XBs xbs, Mdf recvMdf, Id.IT<T> it, int depth) {
    return methsAux(xbs, recvMdf, it).stream().map(cm->freshenMethGens(cm, depth)).toList();
  }
  record MethsCacheKey(XBs xbs, Mdf recvMdf, Id.IT<T> it){}
  Map<MethsCacheKey, List<NormResult>> methsCache = new ConcurrentHashMap<>();
  default List<NormResult> methsAux(XBs xbs, Mdf recvMdf, Id.IT<T> it) {
    var cacheKey = new MethsCacheKey(xbs, recvMdf, it);
    // Can't use computeIfAbsent here because concurrent modification thanks to mutual recursion :-(
    var cached = methsCache.get(cacheKey);
    if (cached != null) { return cached; }
    List<NormResult> cms = Stream.concat(
      cMsOf(recvMdf, it).stream(),
      itsOf(it).stream().flatMap(iti->methsAux(xbs, recvMdf, iti).stream())
    ).toList();
    var res = prune(xbs, cms, posOf(it));
    methsCache.put(cacheKey, res);
    return res;
  }

  default List<NormResult> prune(XBs xbs, List<NormResult> normed, Optional<Pos> lambdaPos) {
    /*
    prune(CMs) = pruneAux(CMs1)..pruneAux(CMsn)
      where CMs1..CMsn = groupByM(norm(CMs)) //groupByM(CMs)=CMss groups for the same m,n
     */
    var seen = new HashSet<CM>();
    var cmsMap = normed.stream()
      .filter(n->seen.add(n.cm()))
      .collect(Collectors.groupingBy(n->n.cm().name()));
    return cmsMap.values().stream()
      .map(ns->pruneAux(xbs, ns, lambdaPos, ns.size()+1))
      .toList();
  }

  default NormResult pruneAux(XBs xbs, List<NormResult> normedCMs, Optional<Pos> lambdaPos, int limit) {
    if(limit==0){
      throw Fail.uncomposableMethods(normedCMs.stream()
        .map(ncm->Fail.conflict(ncm.cm.pos(), ncm.restoreMethodGens().toStringSimplified()))
        .toList()
      ).pos(lambdaPos);
    }
    /*
    pruneAux(CM) = CM
    pruneAux(CM0..CMn)= pruneAux(CMs) where
      CMs=allCases(CM0..CMn)
      n > 0
      CMs.size < n //else error
     */
    assert !normedCMs.isEmpty();
    var first=normedCMs.getFirst();
    if (normedCMs.size() == 1) { return first; }
    var nextCms=normedCMs.stream().skip(1)
      .peek(cmi->{
        var sameGens = first.cm.sig().gens().equals(cmi.cm.sig().gens());
        var sameBounds = first.cm.sig().bounds().equals(cmi.cm.sig().bounds());
        if (!sameGens || !sameBounds) {
          throw Fail.uncomposableMethods(List.of(
            Fail.conflict(first.cm.pos(), first.restoreMethodGens().toStringSimplified()),
            Fail.conflict(cmi.cm.pos(), cmi.restoreMethodGens().toStringSimplified())
          )).pos(lambdaPos);
        }
      })
      .filter(cmi->!firstIsMoreSpecific(xbs, first.cm, cmi.cm) && !firstIsMoreSpecific(xbs, plainCM(first.cm), plainCM(cmi.cm)))
      .toList();

    return pruneAux(xbs, Push.of(nextCms,first), lambdaPos, limit - 1);
  }

  /*default List<CM> allCases(List<CM> cms) {

    allCases(CMs) = CMs.enumerate().permutations(2)
      .filter(<<i,m1>,<j,m2>> -> i < j)
      .map(<<i,m1>,<j,m2>>->selectMoreSpecific(m1,m2)).toList()

    var res = new LinkedHashSet<CM>();
    for (int i : Range.of(cms)) {
      for (int j : Range.of(cms)) {
        if (i <= j) { continue; }
        res.add(selectMoreSpecific(cms.get(i), cms.get(j)));
      }
    }
    return res.stream().toList();
  }*/

  // TODO: Write some tests to check that this is transitive
  /*default CM selectMoreSpecific(CM a, CM b) {
    return selectMoreSpecificAux(a,b)
      .or(()->selectMoreSpecificAux(b,a))
      .orElseThrow(()->Fail.uncomposableMethods(a.c(), b.c()));
  }*/
  default boolean firstIsMoreSpecific(XBs xbs, CM a, CM b) {
      /*
      selectMoreSpecific(CM1,CM2) = CMi
        where
          CMi = Ci[Tsi] . MDF m[Xs](G):Ti e?i //Xs (not Xsi) requires the same (normed) Xs
          {j} = {1,2}\i
          not Ds|- Cj[Tsj]<=Ci[Tsi]
          either
           - Ds|- Ci[Tsi]<=Cj[Tsj] and Ds|- Ti<=Tj
           - e?j is empty and Ti = Tj//only not derm on syntactically eq
           - e?j is empty, Ds|- Ti<=Tj and not Ds|- Tj<=Ti
       */
    assert a.name().equals(b.name());
    assert a.sig().gens().equals(b.sig().gens());
    assert a.sig().bounds().equals(b.sig().bounds());
    var xbs_ = xbs.addBounds(a.sig().gens(), a.sig().bounds());
    var ta = new T(Mdf.mut, a.c());
    var tb = new T(Mdf.mut, b.c());
    if(tryIsSubType(xbs, tb, ta)){ return false; }
    var ok=a.sig().gens().equals(b.sig().gens())
      && a.sig().ts().equals(b.sig().ts())
      && a.mdf()==b.mdf();
    if(!ok){ return false; }

    var isSubType = tryIsSubType(xbs, ta, tb) && tryIsSubType(xbs_, a.ret(), b.ret());
    if(isSubType){ return true; }

    var is1AbsAndRetEq = b.isAbs() && a.ret().equals(b.ret());
    if(is1AbsAndRetEq){ return true; }

    var is1AbsAndRetSubtype = b.isAbs()
      && tryIsSubType(xbs_, a.ret(), b.ret())
      && !tryIsSubType(xbs_, b.ret(), a.ret());
    if(is1AbsAndRetSubtype){ return true; }

    return false;
  }

  record RenameGens(Map<Id.GX<T>,Id.GX<T>> subst) implements CloneVisitor {
    public Id.GX<T> visitGX(Id.GX<T> t) {
      var thisSubst = subst.get(t);
      if (thisSubst != null) { return thisSubst; }
      return t;
    }

    @Override public ast.E.Sig visitSig(ast.E.Sig e) {
      var xbs = e.bounds().entrySet().stream().collect(Collectors.toMap(
        kv->{
          var thisSubst = subst.get(kv.getKey());
          if (thisSubst != null) { return thisSubst; }
          return kv.getKey();
        },
        Map.Entry::getValue
      ));
      return new ast.E.Sig(
        e.gens().stream().map(this::visitGX).toList(),
        xbs,
        e.ts().stream().map(this::visitT).toList(),
        visitT(e.ret()),
        e.pos()
      );
    }
  }

  record NormResult(CM cm, Map<Id.GX<T>,Id.GX<T>> restoreSubst) {
    public CM restoreMethodGens() {
      var newSig = new RenameGens(restoreSubst).visitSig(cm.sig());
      if (newSig.gens().equals(List.of(new Id.GX<>("R"))) && newSig.ts().size() == 1 && newSig.ts().getFirst().toString().equals("mut base.ControlFlowMatch[R, R]")){
        System.out.println(newSig);
      }
      return cm.withSig(newSig);
    }
  }
  default NormResult norm(CM cm) {
    @SuppressWarnings("unchecked")
    var gxs=(Id.GX<T>[]) cm.sig().gens().toArray(Id.GX[]::new);

    var normaliser = new Normaliser<T>(0);
    Map<Id.GX<T>,Id.GX<T>> subst = new HashMap<>();
    Map<Id.GX<T>,Id.GX<T>> restore = new HashMap<>();
    var normedNames = normaliser.normalisedNames(gxs.length);
    Streams.zip(gxs, normedNames).forEach((original, normed)->{
      subst.put(original, normed);
      var plain = original.name().endsWith("$") ? original : new Id.GX<T>(original.name()+"$0");
      restore.put(normed, plain);
    });
    var newSig = new RenameGens(subst).visitSig(cm.sig());
    return new NormResult(cm.withSig(newSig), Collections.unmodifiableMap(restore));
  }

  /**
   * Normalised CMs are required for 5a, but the rest of the type system needs fresh names.
   */
  default CM freshenMethGens(NormResult normedCM, int depth) {
    var cm = normedCM.restoreMethodGens();
    var gxs=cm.sig().gens();
    var refresher = new Refresher<T>(depth);
    Map<Id.GX<T>,Id.GX<T>> subst = refresher.substitutes(gxs);
    var newSig=new RenameGens(subst).visitSig(cm.sig());
    return cm.withSig(newSig);
  }
}
