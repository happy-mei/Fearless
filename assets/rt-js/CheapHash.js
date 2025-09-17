export class rt$$CheapHash {
  constructor() {
    this.result = 1;
  }

  static $self = new rt$$CheapHash();

  compute$mut() {
    return BigInt(this.result); // Return as JS BigInt to mimic Java Long
  }

  nat$mut(x) {
    this.result = ((this.result << 5) - this.result + rt$$CheapHash.longHash(x)) | 0;
    return this;
  }

  int$mut(x) {
    this.result = ((this.result << 5) - this.result + rt$$CheapHash.longHash(x)) | 0;
    return this;
  }

  float$mut(x) {
    this.result = ((this.result << 5) - this.result + rt$$CheapHash.doubleHash(x)) | 0;
    return this;
  }

  byte$mut(x) {
    this.result = ((this.result << 5) - this.result + (x & 0xFF)) | 0;
    return this;
  }

  str$mut(x) {
    const utf8Bytes = x.utf8(); // assuming x.utf8() returns Uint8Array
    let hash = 0;
    for (let i = 0; i < utf8Bytes.length; i++) {
      hash = ((hash << 5) - hash + utf8Bytes[i]) | 0;
    }
    this.result = ((this.result << 5) - this.result + hash) | 0;
    return this;
  }

  hash$mut(x) {
    return x.hash$read(this);
  }

  // Helpers to mimic Java hashCode
  static longHash(x) {
    const n = BigInt(x);
    return Number(n ^ (n >> 32n)) | 0;
  }

  static doubleHash(x) {
    // Convert double to IEEE-754 64-bit bits and hash like Java Double.hashCode
    const buffer = new ArrayBuffer(8);
    new DataView(buffer).setFloat64(0, x, false); // big-endian
    const high = new DataView(buffer).getUint32(0);
    const low = new DataView(buffer).getUint32(4);
    return (high ^ low) | 0;
  }
}
