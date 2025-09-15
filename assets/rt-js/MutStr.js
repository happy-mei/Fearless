import { rt$$Str } from "./Str.js";
import { rt$$NativeRuntime } from "./NativeRuntime.js";
import { base$$True_0, base$$False_0, base$$Void_0 } from "../base/index.js";

export class rt$$MutStr {
  constructor(str) {
    // Start with an empty buffer, grow as needed
    this._buffer = new Uint8Array(16);
    this._length = 0; // actual bytes used
    this._graphemes = null;
    if (str) {
      this.put(str);
    }
  }

  // Return a Str-like object with the current bytes
  utf8() {
    return this._buffer.slice(0, this._length);
  }

  // Compute or return cached grapheme indices
  graphemes() {
    if (this._graphemes) return this._graphemes;
    this._graphemes = rt$$NativeRuntime.indexString(this.utf8());
    return this._graphemes;
  }

  // Is the string empty?
  isEmpty$read() {
    return this._length === 0 ? base$$True_0.$self : base$$False_0.$self;
  }

  // Mutable concatenation
  $plus$mut(other$) {
    const other = other$.str$read();
    this.put(other);
    return this;
  }

  append$mut(other$) {
    this.$plus$mut(other$);
    return base$$Void_0.$self;
  }

  clear$mut() {
    this._buffer = new Uint8Array(16);
    this._length = 0;
    this._graphemes = null;
    return base$$Void_0.$self;
  }

  str$read() {
    return rt$$Str.fromTrustedUtf8(this.utf8());
  }

  // --- private helper ---
  put(str) {
    this._graphemes = null;
    const bytes = str.utf8();
    // Resize buffer if needed
    if (this._buffer.length - this._length < bytes.length) {
      const minSizeIncrease = Math.floor(this._buffer.length * 1.5) + 1;
      const newSize = Math.max(minSizeIncrease, this._buffer.length + bytes.length);
      const newBuffer = new Uint8Array(newSize);
      newBuffer.set(this._buffer.slice(0, this._length));
      this._buffer = newBuffer;
    }
    this._buffer.set(bytes, this._length);
    this._length += bytes.length;
  }
}
