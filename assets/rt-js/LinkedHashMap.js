import { base$$Opt_1, base$$Opts_0, base$$Void_0, base$$True_0, base$$False_0 } from "../base/index.js";
import { base$$flows$$Flow_0 } from "../base/flows/Flow_0.js";
import { SpliteratorFlowOp } from "./flows/SpliteratorFlowOp.js";

export class rt$$LinkedHashMap {
  constructor(keyEq, hashFn) {
    this.hashFn = hashFn;
    this.keyEq = keyEq;
    this.inner = new Map();
  }

  keyOf(k) {
    return new Key(k, this.hashFn, (k1, k2) => this.keyEq$read(k1, k2));
  }

  get$read(k) {
    const res = this.inner.get(this.keyOf(k).hashCode());
    return res !== undefined ? base$$Opts_0.$self.$hash$imm(res) : base$$Opt_1.$self;
  }
  get$imm(k) { return this.get$read(k); }
  get$mut(k) { return this.get$read(k); }

  $plus$mut(k, v) {
    this.inner.set(this.keyOf(k).hashCode(), v);
    return this;
  }

  remove$mut(k) {
    const key = this.keyOf(k).hashCode();
    const res = this.inner.get(key);
    this.inner.delete(key);
    return res !== undefined ? base$$Opts_0.$self.$hash$imm(res) : base$$Opt_1.$self;
  }

  clear$mut() {
    this.inner.clear();
    return base$$Void_0.$self;
  }

  keyEq$read(k1, k2) {
    const res = this.keyEq.$hash$read(k1, k2);
    return res instanceof Bool_0 ? res : (res ? base$$True_0.$self : base$$False_0.$self);
  }

  isEmpty$read() {
    return this.inner.size === 0 ? base$$True_0.$self : base$$False_0.$self;
  }

  put$mut(k, v) {
    return this.$plus$mut(k, v);
  }

  keys$read() {
    const keys = Array.from(this.inner.keys());
    return base$$flows$$Flow_0.$self.fromOp$imm(SpliteratorFlowOp.of(keys), this.inner.size);
  }

  values$mut() {
    const values = Array.from(this.inner.values());
    return base$$flows$$Flow_0.$self.fromMutSource$imm(SpliteratorFlowOp.of(values), this.inner.size);
  }

  values$read() { return this.values$mut(); }
  values$imm() { return this.values$read(); }

  flowMut$mut() {
    return base$$flows$$Flow_0.$self.fromMutSource$imm(SpliteratorFlowOp.of(this.mapToEntries()), this.inner.size);
  }

  flow$read() {
    return base$$flows$$Flow_0.$self.fromOp$imm(SpliteratorFlowOp.of(this.mapToEntries()), this.inner.size);
  }

  flow$imm() { return this.flow$read(); }

  mapToEntries() {
    const entries = [];
    for (const [keyHash, value] of this.inner.entries()) {
      entries.push({
        key$read: () => keyHash, // Note: In JS we use hash as key
        value$read: () => value,
        value$mut: () => value
      });
    }
    return entries;
  }
}

// Key wrapper
class Key {
  constructor(k, hashFn, keyEqFn) {
    this.k = k;
    this.hashFn = hashFn;
    this.keyEqFn = keyEqFn;
    this._hash = null;
  }

  hashCode() {
    if (this._hash === null) {
      const ch = this.hashFn.$hash$read(this.k);
      this._hash = Math.floor(ch.compute$mut());
    }
    return this._hash;
  }

  equals(other) {
    return this.keyEqFn(this.k, other.k) === base$$True_0.$self;
  }
}
