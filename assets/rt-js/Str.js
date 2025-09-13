// rt/Str.js
import { base$$True_0 } from "../base/True_0.js";
import { base$$False_0 } from "../base/False_0.js";
import { base$$_StrHelpers_0 } from "./base/_StrHelpers_0.js";
import { Flow_0 } from "../base/Flow.js";
import { FearlessError } from "./FearlessError.js";

export class rt$$Str {
  constructor() {
    if (this.constructor === rt$$Str) throw new Error("Cannot instantiate interface");
  }

  // --- core interface methods ---

  /** utf8 returns a Uint8Array */
  utf8() { throw new Error("Abstract method"); }

  graphemes() { throw new Error("Abstract method"); }

  utf8$imm() { return this.utf8(); }

  str$read() { return this; }

  $equals$equals$imm(other) {
    const a = this.utf8(), b = other.utf8();
    if (a.length !== b.length) return base$$False_0.$self;
    for (let i = 0; i < a.length; i++) if (a[i] !== b[i]) return base$$False_0.$self;
    return base$$True_0.$self;
  }

  $exclamation$equals$imm(other) {
    return this.$equals$equals$imm(other) === base$$True_0.$self ? base$$False_0.$self : base$$True_0.$self;
  }

  startsWith$imm(other) {
    const a = this.utf8(), b = other.utf8();
    if (b.length > a.length) return base$$False_0.$self;
    for (let i = 0; i < b.length; i++) if (a[i] !== b[i]) return base$$False_0.$self;
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

  assertEq$imm(other) { return base$$_StrHelpers_0.$self.assertEq$imm(this, other); }
  assertEq$imm2(other, msg) { return base$$_StrHelpers_0.$self.assertEq$imm(this, other, msg); }

  isEmpty$read() { return this.utf8().length === 0 ? base$$True_0.$self : base$$False_0.$self; }

  substring$imm(start, end) {
    if (start > end) throw new FearlessError(rt$$Str.fromJavaStr("Start index must be <= end"));
    if (start < 0) throw new FearlessError(rt$$Str.fromJavaStr("Start index >= 0"));
    if (end > this.size$imm()) throw new FearlessError(rt$$Str.fromJavaStr("End index <= size"));
    return new rt$$Str.SubStr(this, start, end);
  }

  charAt$imm(index) { return this.substring$imm(index, index + 1); }

  normalise$imm() {
    const utf8 = this.utf8(); // assume native runtime normalisation
    return rt$$Str.fromTrustedUtf8(utf8);
  }

  // --- static helpers ---
  static fromJavaStr(str) {
    const encoder = new TextEncoder();
    return rt$$Str.fromTrustedUtf8(encoder.encode(str));
  }

  static fromUtf8(utf8) {
    // optionally validate
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
        if (!this._graphemes) this._graphemes = rt$$Str.indexString(this._utf8);
        return this._graphemes;
      }
    };
  }

  /** simple grapheme indexing for demo */
  static indexString(utf8) {
    // naive: 1 byte per char, real implementation needs proper UTF-8 decoding
    return Array.from({ length: utf8.length }, (_, i) => i);
  }

  // --- SubStr implementation ---
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
      if (!this._graphemes) this._graphemes = rt$$Str.indexString(this._utf8);
      return this._graphemes;
    }
    size$imm() { return this._size; }
    isEmpty$read() { return this._size === 0 ? base$$True_0.$self : base$$False_0.$self; }
  };
}
