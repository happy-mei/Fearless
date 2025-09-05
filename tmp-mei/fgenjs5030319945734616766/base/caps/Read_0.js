import { base$$caps$$FileHandleMode_0 } from "../../base/caps/FileHandleMode_0.js";
import * as rt from "../rt/main.js";

export class base$$caps$$Read_0 extends base$$caps$$FileHandleMode_0 {
  static $self = new base$$caps$$Read_0();

  str$read() {
  return base$$caps$$Read_0.str$read$fun(this);
}

  static str$read$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
