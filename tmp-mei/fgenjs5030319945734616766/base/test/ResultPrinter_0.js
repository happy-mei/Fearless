import { base$$test$$ResultReporter_0 } from "../../base/test/ResultReporter_0.js";
import * as rt from "../rt/main.js";

export class base$$test$$ResultPrinter_0 extends base$$test$$ResultReporter_0 {
  static $self = new base$$test$$ResultPrinter_0();

  $hash$mut(results_m$) {
  return base$$test$$ResultPrinter_0.$hash$mut$fun(results_m$, this);
}

  static $hash$mut$fun(results_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
