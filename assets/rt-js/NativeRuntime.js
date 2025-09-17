import * as wasmGlue from "./libwasm/native_rt.js";

const _textDecoder = typeof TextDecoder !== "undefined" ? new TextDecoder("utf-8") : null;
const _textEncoder = typeof TextEncoder !== "undefined" ? new TextEncoder() : null;
let _wasmReady = false;

export async function ensureWasm() {
  if (_wasmReady) return;
  if (typeof wasmGlue.__wbg_init === "function") {
    try {
      await wasmGlue.__wbg_init();
      _wasmReady = true;
    } catch (e) {
      console.warn("NativeRuntime: wasm init failed:", e);
    }
  }
}

export function _toUint8Array(bufOrStr) {
  if (!bufOrStr) return new Uint8Array(0);
  if (bufOrStr instanceof Uint8Array) return bufOrStr;
  if (bufOrStr instanceof ArrayBuffer) return new Uint8Array(bufOrStr);
  if (typeof bufOrStr === "string") return _textEncoder.encode(bufOrStr);
  if (typeof bufOrStr.utf8 === "function") return _toUint8Array(bufOrStr.utf8());
  throw new TypeError("Expected Uint8Array | ArrayBuffer | string | Str-like object");
}

export function toStringFromUtf8(u8) {
  if (!u8) return "";
  if (!(u8 instanceof Uint8Array)) u8 = _toUint8Array(u8);
  if (!_textDecoder) return Array.from(u8).map(b => String.fromCharCode(b)).join("");
  return _textDecoder.decode(u8);
}

export const rt$$NativeRuntime = {
  ensureWasm,
  _toUint8Array,
  toStringFromUtf8,

  validateStringOrThrow(u8) {
    const bytes = _toUint8Array(u8);
    if (_textDecoder) {
      try {
        const dec = new TextDecoder("utf-8", { fatal: true });
        dec.decode(bytes);
      } catch {
        throw new Error("Invalid UTF-8");
      }
    }
  },

  indexString(u8) {
    const bytes = _toUint8Array(u8);
    if (_wasmReady && typeof wasmGlue.index_string === "function") {
      try {
        const s = toStringFromUtf8(bytes);
        const arr = wasmGlue.index_string(s);
        return arr instanceof Int32Array ? arr : Int32Array.from(arr);
      } catch {
        throw new Error("indexString error");
      }
    }
    const out = new Int32Array(bytes.length);
    for (let i = 0; i < bytes.length; i++) out[i] = i;
    return out;
  },

  normaliseString(u8) {
    const bytes = _toUint8Array(u8);
    if (_wasmReady && typeof wasmGlue.normalise_string === "function") {
      const s = toStringFromUtf8(bytes);
      const normalized = wasmGlue.normalise_string(s);
      return _textEncoder ? _textEncoder.encode(normalized) : Uint8Array.from(Array.from(normalized).map(c => c.charCodeAt(0)));
    }
    return bytes;
  },

  floatToStr(value) {
    const s = String(value);
    return _textEncoder ? _textEncoder.encode(s) : Uint8Array.from(Array.from(s).map(c => c.charCodeAt(0)));
  },

};
