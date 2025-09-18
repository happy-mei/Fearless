import { base$$False_0, base$$True_0 } from "../base/index.js";

export class rt$$Numbers {
  // // exponentiation with BigInt (like pow64 in Java)
  // static pow64(base, expBits) {
  //   base = BigInt(base);
  //   expBits = BigInt(expBits);
  //   let res = 1n;
  //   while (expBits !== 0n) {
  //     if ((expBits & 1n) !== 0n) res *= base;
  //     base *= base;
  //     expBits >>= 1n;
  //   }
  //   return res;
  // }
  //
  // // pow8: byte promoted to BigInt then back to Number (0-255 wraparound)
  // static pow8(base, expBits) {
  //   const result = Numbers.pow64(BigInt(base), BigInt(expBits));
  //   return Number(result & 0xFFn); // simulate byte overflow
  // }
  //
  // integer sqrt for signed Int (BigInt)
  static intSqrt(x) {
    x = BigInt(x);
    if (x < 0n) throw new Error("sqrt of negative Int");
    if (x <= 1n) return x;

    let r = BigInt(Math.floor(Math.sqrt(Number(x))));
    while (true) {
      const q = x / r;
      const nr = (r + q) >> 1n;
      if (nr >= r) break;
      r = nr;
    }
    const rp1 = r + 1n;
    if (x / rp1 >= rp1) r = rp1;
    return r;
  }

  // sqrt for Nat (unsigned)
  static natSqrt(u) {
    u = BigInt.asUintN(64, u);
    if (u === 0n || u === 1n) return u;

    let r = BigInt(Math.floor(Math.sqrt(Number(u))));
    while (true) {
      const q = u / r;
      const nr = (r + q) >> 1n;
      if (nr >= r) break;
      r = nr;
    }
    const rp1 = r + 1n;
    if (u / rp1 >= rp1) r = rp1;
    return r;
  }

  // sqrt for Byte
  static byteSqrt(raw) {
    const x = Number(raw) & 0xFF; // unsigned byte
    const r = Math.floor(Math.sqrt(x));
    return r & 0xFF;
  }

  // convert JS boolean to Fearless Bool object
  static toBool(b) {
    return b ? base$$True_0.$self : base$$False_0.$self;
  }

  // Overflow/Underflow:  just like clocks, numbers silently wrap
  static toInt64(x) {
    const mask64 = (1n << 64n) - 1n;
    x &= mask64;
    if (x >= (1n << 63n)) x -= (1n << 64n);
    return x;
  }

  static toNat64(x) {
    const mask64 = (1n << 64n) - 1n;
    return x & mask64;
  }

}
