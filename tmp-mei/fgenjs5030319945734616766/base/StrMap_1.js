import { base$$LinkedLens_2 } from "../base/LinkedLens_2.js";
import * as rt from "../rt/main.js";

export class base$$StrMap_1 extends base$$LinkedLens_2 {
  static $self = new base$$StrMap_1();

  get$imm(k_m$) {
  return base$$LinkedLens_2.get$imm$fun(k_m$, this);
}


get$mut(k_m$) {
  return base$$LinkedLens_2.get$mut$fun(k_m$, this);
}


get$read(k_m$) {
  return base$$LinkedLens_2.get$read$fun(k_m$, this);
}


keyEq$read(k1_m$, k2_m$) {
  return base$$StrMap_1.keyEq$read$fun(k1_m$, k2_m$, this);
}


isEmpty$read() {
  return base$$Map_2.isEmpty$read$fun(this);
}


map$mut(fImm_m$, fMut_m$, fRead_m$) {
  return base$$LinkedLens_2.map$mut$fun(fImm_m$, fMut_m$, fRead_m$, this);
}


put$imm(k_m$, v_m$) {
  return base$$LinkedLens_2.put$imm$fun(k_m$, v_m$, this);
}


put$mut(k_m$, v_m$) {
  return base$$LinkedLens_2.put$mut$fun(k_m$, v_m$, this);
}


put$read(k_m$, v_m$) {
  return base$$LinkedLens_2.put$read$fun(k_m$, v_m$, this);
}


map$imm(fImm_m$, fRead_m$) {
  return base$$LinkedLens_2.map$imm$fun(fImm_m$, fRead_m$, this);
}


map$read(fImm_m$, fRead_m$) {
  return base$$LinkedLens_2.map$read$fun(fImm_m$, fRead_m$, this);
}

  static keyEq$read$fun(k1_m$, k2_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
