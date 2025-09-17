import { rt$$Str } from "./Str.js";
import { rt$$NativeRuntime } from "./NativeRuntime.js";
import { base$$Void_0 } from "../base/Void_0.js";

// A tiny "Fallible" wrapper to mirror the Java runtime semantics
class Fallible {
  constructor(fn) {
    this._fn = fn;
  }
  apply(res) {
    return this._fn(res);
  }
}

// Detect environment: Node vs browser
const hasNodeBuffer =
  typeof globalThis !== "undefined" &&
  typeof globalThis.Buffer !== "undefined" &&
  typeof globalThis.Buffer.from === "function";

function makeBuffer(arr) {
  if (hasNodeBuffer) {
    return Buffer.from(arr);
  } else {
    return new Uint8Array(arr);
  }
}

export class rt$$UTF8 {
  static $self = new rt$$UTF8();

  fromBytes$imm(utf8Bytes_m$) {
    if (utf8Bytes_m$ instanceof rt.ListK.ByteBufferListImpl) {
      return this.utf8ToStr(utf8Bytes_m$.inner().slice());
    } else if (utf8Bytes_m$ instanceof rt.ListK.ListImpl) {
      return this.utf8ToStr(this.rawListToBuffer(utf8Bytes_m$.inner()));
    } else {
      return this.utf8ToStr(this.listToBuffer(utf8Bytes_m$));
    }
  }

  utf8ToStr(buf) {
    return new Fallible((res) => {
      try {
        let str;
        if (hasNodeBuffer && Buffer.isBuffer(buf)) {
          // Node: Buffer supports utf8 decoding natively
          str = buf.toString("utf8");
        } else {
          // Browser: use TextDecoder
          const decoder = new TextDecoder("utf-8", { fatal: true });
          str = decoder.decode(buf);
        }
        return res.ok$mut(rt$$Str.fromJavaStr(str));
      } catch (e) {
        if (e instanceof rt$$NativeRuntime.StringEncodingError) {
          return res.info$mut(e.info);
        }
        throw e;
      }
    });
  }

  rawListToBuffer(arr) {
    // Normalize signed bytes -> [0..255]
    return makeBuffer(arr.map((b) => (b & 0xff)));
  }

  listToBuffer(list_1) {
    const size = list_1.size$read().intValue();
    const arr = new Array(size);
    let i = 0;
    list_1.iter$mut().for$mut((b) => {
      arr[i++] = b & 0xff;
      return base$$Void_0.$self;
    });
    return makeBuffer(arr);
  }
}
