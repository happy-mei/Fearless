import { base$$Str } from "./Str.js";
import { base$$Void_0 } from "../base/Void_0.js";

// A tiny "Fallible" wrapper to mirror the Java runtime semantics
// res -> ok/info
class Fallible {
  constructor(fn) {
    this._fn = fn;
  }
  apply(res) {
    return this._fn(res);
  }
}

export class UTF8 {
  static $self = new UTF8();

  /**
   * fromBytes: takes a List_1 (Fearless list of bytes) and decodes to UTF-8 string
   */
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
        // Buffer in Node handles utf8 cleanly
        const str = base$$Str.fromUtf8(buf);
        return res.ok$mut(str);
      } catch (e) {
        if (e instanceof rt.NativeRuntime.StringEncodingError) {
          return res.info$mut(e.info);
        }
        throw e;
      }
    });
  }

  rawListToBuffer(arr) {
    // arr is Array<Byte> (0..255 signed values in Java, but in JS we normalize)
    return Buffer.from(arr.map((b) => (b & 0xFF)));
  }

  listToBuffer(list_1) {
    const size = list_1.size$read().intValue();
    const arr = [];
    list_1.iter$mut().for$mut((b) => {
      arr.push(b & 0xFF);
      return base$$Void_0.$self;
    });
    return Buffer.from(arr);
  }
}
