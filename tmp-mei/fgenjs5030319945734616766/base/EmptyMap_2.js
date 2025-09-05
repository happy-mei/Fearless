import { base$$Map_2 } from "../base/Map_2.js";
import * as rt from "../rt/main.js";

export class base$$EmptyMap_2 extends base$$Map_2 {
  static $self = new base$$EmptyMap_2();

  get$read(k_m$) {
  return base$$EmptyMap_2.get$read$fun(k_m$, this);
}


get$imm(k_m$) {
  return base$$EmptyMap_2.get$imm$fun(k_m$, this);
}


keyEq$read(k1_m$, k2_m$) {
  return base$$EmptyMap_2.keyEq$read$fun(k1_m$, k2_m$, this);
}


isEmpty$read() {
  return base$$Map_2.isEmpty$read$fun(this);
}

  static keyEq$read$fun(k1_m$, k2_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static get$imm$fun(k_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static get$read$fun(k_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
