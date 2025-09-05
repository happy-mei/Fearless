import * as rt from "../rt/main.js";
export class base$$Actions_0 {
  static $self = new base$$Actions_0();

  info$imm(info_m$) {
  return base$$Actions_0.info$imm$fun(info_m$, this);
}


ok$imm(x_m$) {
  return base$$Actions_0.ok$imm$fun(x_m$, this);
}


lazy$imm(f_m$) {
  return base$$Actions_0.lazy$imm$fun(f_m$, this);
}

  static lazy$imm$fun(f_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static ok$imm$fun(x_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static info$imm$fun(info_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
