import * as rt from "../rt/main.js";
export class base$$Infos_0 {
  static $self = new base$$Infos_0();

  list$imm(list_m$) {
  return base$$Infos_0.list$imm$fun(list_m$, this);
}


msg$imm(msg_m$) {
  return base$$Infos_0.msg$imm$fun(msg_m$, this);
}


map$imm(map_m$) {
  return base$$Infos_0.map$imm$fun(map_m$, this);
}

  static msg$imm$fun(msg_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static list$imm$fun(list_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static map$imm$fun(map_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
