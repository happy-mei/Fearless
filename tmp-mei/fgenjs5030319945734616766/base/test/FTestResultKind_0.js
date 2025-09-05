import * as rt from "../rt/main.js";
export class base$$test$$FTestResultKind_0 {
  static $self = new base$$test$$FTestResultKind_0();

  passed$imm() {
  return base$$test$$FTestResultKind_0.passed$imm$fun(this);
}


failed$imm(details_m$) {
  return base$$test$$FTestResultKind_0.failed$imm$fun(details_m$, this);
}


skipped$imm() {
  return base$$test$$FTestResultKind_0.skipped$imm$fun(this);
}


errored$imm(details_m$) {
  return base$$test$$FTestResultKind_0.errored$imm$fun(details_m$, this);
}

  static passed$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static skipped$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static failed$imm$fun(details_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static errored$imm$fun(details_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
