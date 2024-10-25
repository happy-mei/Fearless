package program.inference;

import ast.T;
import astFull.E;
import id.Id;
import id.Mdf;
import program.CM;
import program.Program;
import program.typesystem.EMethTypeSystem;
import program.typesystem.XBs;
import utils.Box;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static program.TypeTable.filterByMdf;

record FullMethSig(Id.MethName name, E.Sig sig){
  public static List<FullMethSig> of(Program p, XBs xbs, Mdf recvMdf, List<Id.IT<astFull.T>> its, int depth, Predicate<CM> pred) {
    var nFresh = new Box<>(0);
    var coreIts = its.stream().map(it->it.toAstIT(t->t.toAstTFreshenInfers(nFresh))).distinct().toList();
    var freshName = new Id.DecId(Id.GX.fresh().name(), 0);
    var dec = new T.Dec(new ast.E.Lambda(
      new ast.E.Lambda.LambdaId(freshName, List.of(), Map.of()),
      Mdf.mdf,
      coreIts,
      "fearTmp$",
      List.of(),
      Optional.empty()
    ));
    var p_ = p.withDec(dec);
    return p_.meths(xbs, recvMdf, dec.toIT(), depth).stream()
      .filter(cm->filterByMdf(recvMdf, cm.mdf()))
      .filter(pred)
      .sorted(Comparator.comparingInt(cm-> EMethTypeSystem.inferPriority(recvMdf).indexOf(cm.mdf())))
      .map(m->{
        var sig = m.sig().toAstFullSig();
        var freshGXsSet = IntStream.range(0, nFresh.get()).mapToObj(n->new Id.GX<T>("FearTmp"+n+"$")).collect(Collectors.toSet());
        var restoredArgs = sig.ts().stream().map(t->RefineTypes.regenerateInfers(p, freshGXsSet, t)).toList();
        var restoredRt = RefineTypes.regenerateInfers(p, freshGXsSet, sig.ret());
        var restoredSig = new E.Sig(sig.gens(), sig.bounds(), restoredArgs, restoredRt, sig.pos());
        return new FullMethSig(m.name(), restoredSig);
      })
      .toList();
  }
}
