import { base$$flows$$_SinkDecorator_0 } from "../../base/flows/_SinkDecorator_0.js";
import * as rt from "../rt/main.js";

export class base$$flows$$_OnePerStepSinks_0 extends base$$flows$$_SinkDecorator_0 {
  static $self = new base$$flows$$_OnePerStepSinks_0();

  $hash$imm(sink_m$) {
  return base$$flows$$_OnePerStepSinks_0.$hash$imm$fun(sink_m$, this);
}

  static $hash$imm$fun(sink_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
