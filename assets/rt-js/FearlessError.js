import { toStringFromUtf8 } from "./NativeRuntime.js";

export class FearlessError {
  constructor(info) {
    this.name = "FearlessError";
    this.info = info;
  }

  getMessage() {
    if (!this.info) return "";
    return typeof this.info === "string"
      ? this.info
      : toStringFromUtf8(this.info.utf8 ? this.info.utf8() : this.info);
  }

  toString() {
    return this.getMessage();
  }

  static throwFearlessError(info) {
    throw new FearlessError(info);
  }
}
