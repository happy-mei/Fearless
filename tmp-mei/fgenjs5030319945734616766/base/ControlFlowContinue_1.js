import { base$$ControlFlow_1 } from "../base/ControlFlow_1.js";
import * as rt from "../rt/main.js";

export class base$$ControlFlowContinue_1 extends base$$ControlFlow_1 {
  static $self = new base$$ControlFlowContinue_1();

  match$mut(m_m$) {
  return base$$ControlFlowContinue_1.match$mut$fun(m_m$, this);
}

  static match$mut$fun(m_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
