import { base$$Sealed_0 } from "../../base/Sealed_0.js";
import * as rt from "../rt/main.js";

export class base$$flows$$ActorRes_0 extends base$$Sealed_0 {
  static $self = new base$$flows$$ActorRes_0();

  match$imm(m_m$) {
  return base$$flows$$ActorRes_0.match$imm$fun(m_m$, this);
}


stop$imm() {
  return base$$flows$$ActorRes_0.stop$imm$fun(this);
}


continue$imm() {
  return base$$flows$$ActorRes_0.continue$imm$fun(this);
}

  static match$imm$fun(m_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static continue$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static stop$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
