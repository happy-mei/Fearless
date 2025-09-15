import initSync, * as wasm from "./libwasm/native_rt.js";

// Ensure wasm is initialized by other code (you may call initSync() earlier).
// If wasm is not yet initialized by the host, try to initialize lazily on first use.
let _wasmReady = false;
async function ensureWasm() {
  if (_wasmReady) return;
  if (typeof wasm.__wbg_init === "function") {
    // the glue exports a default async init function named __wbg_init (or initSync)
    try {
      // If wasm already loaded by other module this is a no-op / fast.
      await wasm.__wbg_init();
    } catch (e) {
      // swallow — we'll gracefully degrade to JS alternatives
      // (but keep _wasmReady false so future calls may try again)
      console.warn("NativeRuntime: wasm init failed:", e);
      return;
    }
    _wasmReady = true;
  }
}

// Text encoding/decoding helpers
const _textDecoder = (typeof TextDecoder !== "undefined") ? new TextDecoder("utf-8", { fatal: false }) : null;
const _textEncoder = (typeof TextEncoder !== "undefined") ? new TextEncoder() : null;

function _toUint8Array(bufOrStr) {
  if (bufOrStr == null) return new Uint8Array(0);
  if (bufOrStr instanceof Uint8Array) return bufOrStr;
  if (bufOrStr instanceof ArrayBuffer) return new Uint8Array(bufOrStr);
  if (typeof bufOrStr === "string") {
    if (!_textEncoder) throw new Error("TextEncoder not available");
    return _textEncoder.encode(bufOrStr);
  }
  // If object exposing a .utf8() method (Fearless Str-like), accept it
  if (typeof bufOrStr.utf8 === "function") {
    const u = bufOrStr.utf8();
    return _toUint8Array(u);
  }
  throw new TypeError("Expected Uint8Array | ArrayBuffer | string | Str-like object");
}

function toStringFromUtf8(u8) {
  if (!u8) return "";
  if (!(u8 instanceof Uint8Array)) u8 = _toUint8Array(u8);
  if (!_textDecoder) {
    // fallback
    return Array.from(u8).map(b => String.fromCharCode(b)).join("");
  }
  return _textDecoder.decode(u8);
}

// Runtime error used by other rt modules (the FearlessError class)
import { FearlessError } from "./FearlessError.js";
import { rt$$Str as StrHelper } from "./Str.js"; // your rt.Str implementation

export const rt$$NativeRuntime = {
  // ---- initialization helper ----
  ensureWasm,

  // ---- string helpers ----
  // Validate UTF-8 bytes: throws rt$$NativeRuntime.StringEncodingError on invalid data.
  validateStringOrThrow(utf8Bytes) {
    // Accept Uint8Array or ArrayBuffer or Str-like
    const u8 = _toUint8Array(utf8Bytes);
    // If wasm provides validation/indexing we can use that.
    if (_wasmReady && typeof wasm.index_string === "function") {
      try {
        // wasm.index_string expects a JS string in the glue you showed; convert
        // decoding here may throw if input isn't valid UTF-8.
        // We'll attempt to decode using TextDecoder with fatal=true to detect invalid UTF-8.
        if (_textDecoder) {
          // create a fatal decoder only for validation
          const fatalDecoder = new TextDecoder("utf-8", { fatal: true });
          fatalDecoder.decode(u8);
        } else {
          // no fatal decoder available — try wasm path by converting bytes to string
          // (this may silently accept invalid sequences on some platforms).
          toStringFromUtf8(u8);
        }
        return; // OK
      } catch (e) {
        // Wrap into the same Shape as Java: rt$$NativeRuntime.StringEncodingError
        throw new rt$$NativeRuntime.StringEncodingError("Invalid UTF-8");
      }
    }

    // Fallback: try TextDecoder and catch errors (best-effort).
    if (_textDecoder) {
      try {
        // Note: fatal decoder was attempted above if wasmReady; otherwise do best-effort decode.
        _textDecoder.decode(u8);
        return;
      } catch (e) {
        throw new rt$$NativeRuntime.StringEncodingError("Invalid UTF-8");
      }
    }

    // If nothing available, be permissive (we can't validate).
    return;
  },

  // Index a UTF-8 buffer into grapheme/byte offsets.
  // Returns an Int32Array (mirrors Java int[]). Uses wasm.index_string if available.
  indexString(utf8Bytes) {
    const u8 = _toUint8Array(utf8Bytes);
    // If wasm index_string expects a JS string, convert:
    if (_wasmReady && typeof wasm.index_string === "function") {
      try {
        // Convert bytes -> JS string safely (TextDecoder non-fatal)
        const s = toStringFromUtf8(u8);
        const arr = wasm.index_string(s);
        // wasm glue returns an Int32Array already — normalize:
        if (arr instanceof Int32Array) return arr;
        // If wasm returned something else, try to convert:
        return Int32Array.from(arr);
      } catch (e) {
        // If wasm failed, fall back to naive indexer below
      }
    }

    // Fallback: naive per-byte index (not correct for multi-byte UTF-8 graphemes,
    // but this is better than crashing). You should prefer enabling wasm.
    const out = new Int32Array(u8.length);
    for (let i = 0; i < u8.length; i++) out[i] = i;
    return out;
  },

  // Print helpers. Accept Uint8Array, ArrayBuffer, string, or Str-like object.
  print(utf8Bytes) {
    const text = toStringFromUtf8(_toUint8Array(utf8Bytes));
    if (typeof process !== "undefined" && process.stdout) {
      process.stdout.write(String(text));
    } else if (typeof console !== "undefined") {
      // console.log adds newline; use console.log when appropriate
      console.log(String(text));
    } else {
      // last resort (browser)
      // append to document if available
      if (typeof document !== "undefined") {
        const pre = document.createElement("pre");
        pre.textContent = String(text);
        document.body.appendChild(pre);
      }
    }
  },

  println(utf8Bytes) {
    const text = toStringFromUtf8(_toUint8Array(utf8Bytes));
    if (typeof process !== "undefined" && process.stdout) {
      process.stdout.write(String(text) + "\n");
    } else if (typeof console !== "undefined") {
      console.log(String(text));
    } else {
      if (typeof document !== "undefined") {
        const pre = document.createElement("pre");
        pre.textContent = String(text);
        document.body.appendChild(pre);
      }
    }
  },

  printErr(utf8Bytes) {
    const text = toStringFromUtf8(_toUint8Array(utf8Bytes));
    if (typeof process !== "undefined" && process.stderr) {
      process.stderr.write(String(text));
    } else if (typeof console !== "undefined") {
      console.error(String(text));
    }
  },

  printlnErr(utf8Bytes) {
    const text = toStringFromUtf8(_toUint8Array(utf8Bytes));
    if (typeof process !== "undefined" && process.stderr) {
      process.stderr.write(String(text) + "\n");
    } else if (typeof console !== "undefined") {
      console.error(String(text));
    }
  },

  // Normalize string: use wasm.normalise_string if available; returns Uint8Array
  normaliseString(utf8Bytes) {
    const u8 = _toUint8Array(utf8Bytes);
    // If wasm provides normalise_string that accepts JS string, we convert
    if (_wasmReady && typeof wasm.normalise_string === "function") {
      const s = toStringFromUtf8(u8);
      const normalized = wasm.normalise_string(s);
      // Wasm glue returns JS string — convert to Uint8Array
      return _textEncoder ? _textEncoder.encode(normalized) : Uint8Array.from(Array.from(normalized).map(c => c.charCodeAt(0)));
    }
    // Fallback: identity
    return u8;
  },

  // Float formatting helper: emulate the Java native floatToStr -> returns Uint8Array of UTF-8 bytes
  floatToStr(value) {
    // value might be JS Number or BigInt
    const s = Number.isFinite(value) ? String(value) : String(value); // basic formatting
    return _textEncoder ? _textEncoder.encode(s) : Uint8Array.from(Array.from(s).map(c => c.charCodeAt(0)));
  },

  // Regex wrapper similar to Java's rt$$NativeRuntime.Regex
  Regex: class {
    // pattern can be either a Uint8Array (utf8) or string
    constructor(patternUtf8OrString) {
      // ensure wasm loaded if possible
      this._patternRaw = _toUint8Array(patternUtf8OrString);
      this._patternString = toStringFromUtf8(this._patternRaw);

      // Try to use wasm.compile_regex if available; otherwise fallback to JS RegExp
      if (_wasmReady && typeof wasm.compile_regex === "function") {
        try {
          // compile_regex in wasm glue might return an externref or throw; attempt it
          this._wasmCompiled = wasm.compile_regex(this._patternString);
        } catch (e) {
          // if wasm compile raises, wrap message into an error
          throw new rt$$NativeRuntime.Regex.InvalidRegexError(String(e && e.message ? e.message : e));
        }
      } else {
        // Fall back: create a JS RegExp; assume pattern is a JS-style regex literal.
        // NOTE: fearless patterns might not be the same language; this is best-effort.
        try {
          this._jsRegex = new RegExp(this._patternString);
        } catch (e) {
          throw new rt$$NativeRuntime.Regex.InvalidRegexError(String(e && e.message ? e.message : e));
        }
      }
    }

    doesRegexMatch(utf8Bytes) {
      const text = toStringFromUtf8(_toUint8Array(utf8Bytes));
      // Prefer wasm doesRegexMatch if wasm compiled object exists and wasm exposes a matching function
      if (_wasmReady && this._wasmCompiled && typeof wasm.does_regex_match === "function") {
        // wasm.does_regex_match glue expects (patternString, text) per the glue you showed earlier.
        // If wasm.compile_regex returned an externref object, we cannot pass it directly to does_regex_match
        // using the glue you showed; so call does_regex_match(patternString, text).
        try {
          return wasm.does_regex_match(this._patternString, text);
        } catch (e) {
          // fall back to JS
        }
      }
      if (this._jsRegex) {
        return this._jsRegex.test(text);
      }
      // If nothing works, return false
      return false;
    }

    // nested InvalidRegexError class
    static InvalidRegexError = class extends FearlessError {
      constructor(message) {
        // Use Info-like message if rt.Str helper exists; best-effort
        const infoStr = StrHelper.fromJavaStr ? StrHelper.fromJavaStr(String(message)) : { str$imm() { return StrHelper.fromJavaStr(String(message)).utf8(); } };
        super(infoStr);
      }
    };
  },

  // Regex.InvalidRegexError alias for compat with Java name
  // Regex_InvalidRegexError: null // set below
};

// Expose the nested InvalidRegexError to match Java naming
// rt$$NativeRuntime.Regex._InvalidRegexError = rt$$NativeRuntime.Regex.InvalidRegexError;
// rt$$NativeRuntime.Regex_InvalidRegexError = rt$$NativeRuntime.Regex.InvalidRegexError;

rt$$NativeRuntime.toStringFromUtf8 = toStringFromUtf8;
// A small local error class used in this module if you need it
rt$$NativeRuntime.StringEncodingError = class extends FearlessError {
  constructor(message) {
    const msgStr = StrHelper.fromJavaStr ? StrHelper.fromJavaStr(String(message)) : { str$imm() { return StrHelper.fromJavaStr(String(message)).utf8(); } };
    super(msgStr);
  }
};


// import { FearlessError } from "./FearlessError.js";
// import { rt$$Str as StrHelper } from "./Str.js"; // your rt.Str implementation
//
// // Text encoding/decoding helpers
// const _textDecoderFatal = new TextDecoder("utf-8", { fatal: true });
// const _textDecoder = new TextDecoder("utf-8"); // forgiving version
// const _textEncoder = new TextEncoder();
//
// function _toUint8Array(bufOrStr) {
//   if (bufOrStr == null) return new Uint8Array(0);
//   if (bufOrStr instanceof Uint8Array) return bufOrStr;
//   if (bufOrStr instanceof ArrayBuffer) return new Uint8Array(bufOrStr);
//   if (typeof bufOrStr === "string") return _textEncoder.encode(bufOrStr);
//   if (typeof bufOrStr.utf8 === "function") return _toUint8Array(bufOrStr.utf8());
//   throw new TypeError("Expected Uint8Array | ArrayBuffer | string | Str-like object");
// }
//
// function toStringFromUtf8(u8) {
//   if (!(u8 instanceof Uint8Array)) u8 = _toUint8Array(u8);
//   return _textDecoder.decode(u8);
// }

// export const rt$$NativeRuntime = {
//   // ---- string helpers ----
//   validateStringOrThrow(utf8Bytes) {
//     const u8 = _toUint8Array(utf8Bytes);
//     try {
//       _textDecoderFatal.decode(u8);
//     } catch {
//       throw new rt$$NativeRuntime.StringEncodingError("Invalid UTF-8");
//     }
//   },
//
//   indexString(utf8Bytes) {
//     const s = toStringFromUtf8(_toUint8Array(utf8Bytes));
//     const seg = new Intl.Segmenter("en", { granularity: "grapheme" });
//     return Int32Array.from([...seg.segment(s)].map(seg => seg.index));
//   },
//
//   print(utf8Bytes) {
//     const text = toStringFromUtf8(_toUint8Array(utf8Bytes));
//     if (typeof process !== "undefined" && process.stdout) {
//       process.stdout.write(String(text));
//     } else {
//       console.log(String(text));
//     }
//   },
//
//   println(utf8Bytes) {
//     const text = toStringFromUtf8(_toUint8Array(utf8Bytes));
//     if (typeof process !== "undefined" && process.stdout) {
//       process.stdout.write(String(text) + "\n");
//     } else {
//       console.log(String(text));
//     }
//   },
//
//   printErr(utf8Bytes) {
//     const text = toStringFromUtf8(_toUint8Array(utf8Bytes));
//     if (typeof process !== "undefined" && process.stderr) {
//       process.stderr.write(String(text));
//     } else {
//       console.error(String(text));
//     }
//   },
//
//   printlnErr(utf8Bytes) {
//     const text = toStringFromUtf8(_toUint8Array(utf8Bytes));
//     if (typeof process !== "undefined" && process.stderr) {
//       process.stderr.write(String(text) + "\n");
//     } else {
//       console.error(String(text));
//     }
//   },
//
//   normaliseString(utf8Bytes) {
//     const s = toStringFromUtf8(_toUint8Array(utf8Bytes));
//     const normalized = s.normalize("NFC");
//     return _textEncoder.encode(normalized);
//   },
//
//   floatToStr(value) {
//     const s = String(value);
//     return _textEncoder.encode(s);
//   },
//
//   Regex: class {
//     constructor(patternUtf8OrString) {
//       this._patternRaw = _toUint8Array(patternUtf8OrString);
//       this._patternString = toStringFromUtf8(this._patternRaw);
//       try {
//         this._jsRegex = new RegExp(this._patternString);
//       } catch (e) {
//         throw new rt$$NativeRuntime.Regex.InvalidRegexError(String(e.message || e));
//       }
//     }
//
//     doesRegexMatch(utf8Bytes) {
//       const text = toStringFromUtf8(_toUint8Array(utf8Bytes));
//       return this._jsRegex.test(text);
//     }
//
//     static InvalidRegexError = class extends FearlessError {
//       constructor(message) {
//         const infoStr = StrHelper.fromJavaStr
//           ? StrHelper.fromJavaStr(String(message))
//           : { str$imm() { return StrHelper.fromJavaStr(String(message)).utf8(); } };
//         super(infoStr);
//       }
//     };
//   }
// };
//
// // Expose for compat
// rt$$NativeRuntime.Regex_InvalidRegexError = rt$$NativeRuntime.Regex.InvalidRegexError;
//
// // Local error class
// rt$$NativeRuntime.StringEncodingError = class extends FearlessError {
//   constructor(message) {
//     const infoStr = StrHelper.fromJavaStr
//       ? StrHelper.fromJavaStr(String(message))
//       : { str$imm() { return StrHelper.fromJavaStr(String(message)).utf8(); } };
//     super(infoStr);
//   }
// };
