import { base$$F_2 } from "../../base/F_2.js";
import * as rt from "../rt/main.js";

export class base$$test$$FTestRunner_0 extends base$$F_2 {
  static $self = new base$$test$$FTestRunner_0();

  $hash$read(sys_m$) {
  return base$$test$$FTestRunner_0.$hash$read$fun(sys_m$, this);
}

  static $hash$read$fun(sys_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
