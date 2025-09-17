import { base$$True_0, base$$False_0 } from "../base/index.js";
import { rt$$NativeRuntime } from "./NativeRuntime.js";
import { FearlessError } from "./FearlessError.js";
import { ByteBufferListImpl } from "./ListK.js";

export class rt$$Str {
  constructor() {
    if (new.target === rt$$Str) throw new Error("Cannot instantiate interface rt$$Str directly");
  }

  // --- Abstract methods ---
  utf8() { throw new Error("Abstract method"); }       // returns Uint8Array
  graphemes() { throw new Error("Abstract method"); }  // returns Int32Array
  utf8$imm() {
    return new ByteBufferListImpl(this.utf8());
  }
  str$read() { return this; }

  static wrap(array) {
    // Accepts a JS Array or TypedArray of numbers (0–255)
    return new Uint8Array(array);
  }

  $equals$equals$imm(other) {
    const a = this.utf8(), b = other.utf8();
    if (a.length !== b.length) return base$$False_0.$self;
    for (let i = 0; i < a.length; i++) {
      if (a[i] !== b[i]) {
        return base$$False_0.$self;
      }
    }
    return base$$True_0.$self;
  }

  $exclamation$equals$imm(other) {
    return this.$equals$equals$imm(other) === base$$True_0.$self ? base$$False_0.$self : base$$True_0.$self;
  }

  startsWith$imm(other) {
    const a = this.utf8(), b = other.utf8();
    if (b.length > a.length) return base$$False_0.$self;
    for (let i = 0; i < b.length; i++) {
      if (a[i] !== b[i]) {
        return base$$False_0.$self;
      }
    }
    return base$$True_0.$self;
  }

  $plus$imm(other) {
    const a = this.utf8(), b = other.str$read().utf8();
    const res = new Uint8Array(a.length + b.length);
    res.set(a);
    res.set(b, a.length);
    return rt$$Str.fromTrustedUtf8(res);
  }

  size$imm() { return this.graphemes().length; }
  isEmpty$read() { return this.utf8().length === 0 ? base$$True_0.$self : base$$False_0.$self; }

  substring$imm(start, end) {
    if (start > end) throw new FearlessError(rt$$Str.fromJsStr("Start index must be <= end"));
    if (start < 0) throw new FearlessError(rt$$Str.fromJsStr("Start index >= 0"));
    if (end > this.size$imm()) throw new FearlessError(rt$$Str.fromJsStr("End index <= size"));
    return new rt$$Str.SubStr(this, start, end);
  }

  charAt$imm(index) { return this.substring$imm(index, index + 1); }

  normalise$imm() {
    const utf8 = rt$$NativeRuntime.normaliseString(this.utf8());
    return rt$$Str.fromTrustedUtf8(utf8);
  }

  /** Convert to native JS string */
  toJsString() {
    return rt$$NativeRuntime.toStringFromUtf8(this.utf8());
  }

  // --- Static helpers ---
  static fromJsStr(str) {
    const encoder = new TextEncoder();
    return rt$$Str.fromTrustedUtf8(encoder.encode(str));
  }

  static fromUtf8(utf8) {
    rt$$NativeRuntime.validateStringOrThrow(utf8);
    return rt$$Str.fromTrustedUtf8(utf8);
  }

  static fromTrustedUtf8(utf8) {
    return new class extends rt$$Str {
      constructor() {
        super();
        this._utf8 = utf8;
        this._graphemes = null;
      }
      utf8() { return this._utf8; }
      graphemes() {
        if (!this._graphemes) this._graphemes = rt$$NativeRuntime.indexString(this._utf8);
        return this._graphemes;
      }
    };
  }

  /** Substring implementation */
  static SubStr = class extends rt$$Str {
    constructor(parent, start, end) {
      super();
      const graphemes = parent.graphemes();
      const utf8 = parent.utf8();
      const idxStart = start >= graphemes.length ? utf8.length : graphemes[start];
      const idxEnd = end >= graphemes.length ? utf8.length : graphemes[end];
      this._utf8 = utf8.slice(idxStart, idxEnd);
      this._size = end - start;
      this._graphemes = null;
    }
    utf8() { return this._utf8; }
    graphemes() {
      if (!this._graphemes) this._graphemes = rt$$NativeRuntime.indexString(this._utf8);
      return this._graphemes;
    }
    size$imm() { return this._size; }
    isEmpty$read() { return this._size === 0 ? base$$True_0.$self : base$$False_0.$self; }
  };
}
