import * as rt from "../rt/main.js";
export class base$$ControlFlow_0 {
  static $self = new base$$ControlFlow_0();

  continue$imm() {
  return base$$ControlFlow_0.continue$imm$fun(this);
}


return$imm(returnValue_m$) {
  return base$$ControlFlow_0.return$imm$fun(returnValue_m$, this);
}


breakWith$imm() {
  return base$$ControlFlow_0.breakWith$imm$fun(this);
}


continueWith$imm() {
  return base$$ControlFlow_0.continueWith$imm$fun(this);
}


break$imm() {
  return base$$ControlFlow_0.break$imm$fun(this);
}

  static continue$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static break$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static continueWith$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static breakWith$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static return$imm$fun(returnValue_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
