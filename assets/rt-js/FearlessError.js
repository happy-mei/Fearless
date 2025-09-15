// rt/FearlessError.js
import { base$$Info_0 } from "../base/Info_0.js";
import { rt$$Str } from "./Str.js";

export class FearlessError extends Error {
  /**
   * @param {base$$Info_0} info - the Fearless Info object
   */
  constructor(info) {
    super(); // call Error constructor
    this.name = "FearlessError";
    this.info = info; // store the Info object
  }

  /** getMessage equivalent */
  getMessage() {
    // info.str$imm() returns base$$Str_0 object
    // .utf8() returns a Uint8Array
    // rt$$Str.toJsStr converts it to JS string
    return rt$$Str.toJsStr(this.info.str$imm().utf8());
  }

  /** toString equivalent */
  toString() {
    return this.getMessage();
  }
}
