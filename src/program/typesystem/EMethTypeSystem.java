package program.typesystem;

import ast.E;
import failure.Res;
import utils.Bug;

interface EMethTypeSystem extends ETypeSystem{
  default Res visitMCall(E.MCall e){
    /*
    G|- e0 m[Ts](e1..en) : T    (Call-T)
  where
    G|-ei : Ti forall i in 0..n
    T0 = MDF0 C0[Ts0]
    MDF0 < MDF
    MDF m[Xs](x1:T1..xn:Tn):T _ in multiMeth(MDF0 C0[Ts0]{},m[Ts],n)
     */



    throw Bug.todo();
  }


}
