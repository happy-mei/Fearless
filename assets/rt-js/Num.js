import { base$$False_0, base$$True_0 } from "../base/index.js";

export class rt$$Num {
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
