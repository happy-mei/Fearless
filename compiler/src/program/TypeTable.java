package program;

import ast.T;
import files.Pos;
import id.Id;
import id.Mdf;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TypeTable {
  Id.Dec of(Id.DecId dec);
  boolean isInlineDec(Id.DecId d);
  List<Id.IT<T>> itsOf(Id.IT<T> t);
  /** with t=C[Ts]  we do  C[Ts]<<Ms[Xs=Ts],*/
  CM plainCM(CM fancyCM);
  Set<Id.GX<T>> gxsOf(Id.IT<T> t);
  Program withDec(T.Dec d);
  List<ast.E.Lambda> lambdas();
  Optional<Pos> posOf(Id.IT<T> t);
  /** Produce a clone of Program without any cached data */
  Program shallowClone();
  TypeSystemFeatures tsf();

  static boolean filterByMdf(Mdf mdf, Mdf mMdf) {
    assert !mdf.isMdf();
    if (mdf.is(Mdf.iso, Mdf.mut, Mdf.recMdf, Mdf.mdf)) { return true; }
    if (mdf.isMutH() && !mMdf.isIso()) { return true; }
    return mdf.is(Mdf.imm, Mdf.read, Mdf.readImm, Mdf.readH) && mMdf.is(Mdf.imm, Mdf.read, Mdf.readH, Mdf.recMdf);
  }

  default Id.IT<T> liftIT(Id.IT<astFull.T>it){
    var ts = it.ts().stream().map(astFull.T::toAstT).toList();
    return new Id.IT<>(it.name(), ts);
  }
}
