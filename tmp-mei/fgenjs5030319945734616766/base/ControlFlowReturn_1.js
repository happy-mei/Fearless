import { base$$ControlFlow_1 } from "../base/ControlFlow_1.js";
import * as rt from "../rt/main.js";

export class base$$ControlFlowReturn_1 extends base$$ControlFlow_1 {
  static $self = new base$$ControlFlowReturn_1();

  match$mut(m_m$) {
  return base$$ControlFlowReturn_1.match$mut$fun(m_m$, this);
}


value$mut() {
  return base$$ControlFlowReturn_1.value$mut$fun(this);
}

  static match$mut$fun(m_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static value$mut$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
