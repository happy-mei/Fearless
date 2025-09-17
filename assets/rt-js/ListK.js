import { base$$Void_0, base$$Opt_1, base$$Opts_0, base$$True_0, base$$False_0, base$$List_1 } from "../base/index.js";

export const rt$$ListK = {
  $self: null, // will set below

  asShallowClone(list, f) {
    if (list instanceof ListImpl) {
      return new ListImpl([...list.inner]);
    }
    if (list instanceof ByteBufferListImpl) {
      return list;
    }
    return list.as$read(f);
  },

  // Convenience $hash$imm methods
  $hash$imm(...elements) {
    return new ListImpl([...elements]);
  },

  $hash$imm0() {
    return new ListImpl([]);
  },

  fromLList$imm(llist) {
    const arr = [];
    for (const e of llist.iter$mut()) arr.push(e);
    return new ListImpl(arr);
  },

  withCapacity$imm(n) {
    if (n > Number.MAX_SAFE_INTEGER) {
      throw new Error(`Lists may not have a capacity greater than ${Number.MAX_SAFE_INTEGER}`);
    }
    return new ListImpl(new Array(n));
  }
};

rt$$ListK.$self = rt$$ListK;

// --------------------
// ListImpl
// --------------------
export class ListImpl {
  constructor(inner = []) {
    this.inner = inner;
  }

  get$imm(i) { return this.inner[i]; }
  get$read(i) { return this.inner[i]; }
  get$mut(i) { return this.inner[i]; }

  add$mut(e) { this.inner.push(e); return base$$Void_0.$self; }
  $plus$mut(e) { this.inner.push(e); return this; }

  addAll$mut(other) {
    this.inner.push(...other.inner);
    return base$$Void_0.$self;
  }

  takeFirst$mut() {
    if (this.inner.length === 0) return base$$Opt_1.$self;
    return base$$Opts_0.$self.$hash$imm(this.inner.shift());
  }

  tryGet$imm(i) {
    return i >= this.inner.length ? base$$Opt_1.$self : base$$Opts_0.$self.$hash$imm(this.inner[i]);
  }
  tryGet$read(i) { return this.tryGet$imm(i); }
  tryGet$mut(i) { return this.tryGet$imm(i); }

  iter$imm() { return base$$List_1.iter$imm$fun(this); }
  iter$read() { return base$$List_1.iter$read$fun(this); }
  iter$mut() { return base$$List_1.iter$mut$fun(this); }

  flow$imm() { return base$$List_1.flow$imm$fun(this); }
  flow$read() { return base$$List_1.flow$read$fun(this); }
  flow$mut() { return base$$List_1.flow$mut$fun(this); }

  isEmpty$read() { return this.inner.length === 0 ? base$$True_0.$self : base$$False_0.$self; }
  clear$mut() { this.inner.length = 0; return base$$Void_0.$self; }

  subList$read(from, to) { return base$$List_1.subList$read$fun(from, to, this); }
  as$read(f) { return base$$List_1.as$read$fun(f, this); }

  size$read() { return this.inner.length; }

  _flowimm$imm(start, end) { return base$$List_1._flowimm$imm$fun(start, end, this); }
  _flowread$read(start, end) { return base$$List_1._flowread$read$fun(start, end, this); }
}

// --------------------
// ByteBufferListImpl
// --------------------
export class ByteBufferListImpl {
  constructor(inner) {
    this.inner = inner; // assume some JS typed array or similar
  }

  get$imm(i) { return this.inner[i]; }
  get$read(i) { return this.inner[i]; }
  get$mut() { throw new Error("Unreachable code"); }

  tryGet$imm(i) { return i >= this.inner.length ? base$$Opt_1.$self : base$$Opts_0.$self.$hash$imm(this.inner[i]); }
  tryGet$read(i) { return this.tryGet$imm(i); }
  tryGet$mut() { throw new Error("Unreachable code"); }

  iter$imm() { return base$$List_1.iter$imm$fun(this); }
  iter$read() { return base$$List_1.iter$read$fun(this); }
  iter$mut() { throw new Error("Unreachable code"); }

  flow$imm() { return base$$List_1.flow$imm$fun(this); }
  flow$read() { return base$$List_1.flow$read$fun(this); }
  flow$mut() { throw new Error("Unreachable code"); }

  isEmpty$read() { return this.inner.length === 0 ? base$$True_0.$self : base$$False_0.$self; }
  clear$mut() { throw new Error("Unreachable code"); }

  subList$read(from, to) { return base$$List_1.subList$read$fun(from, to, this); }
  as$read(f) { return base$$List_1.as$read$fun(f, this); }

  size$read() { return this.inner.length; }

  add$mut() { throw new Error("Unreachable code"); }
  $plus$mut() { throw new Error("Unreachable code"); }

  _flowimm$imm(start, end) { return base$$List_1._flowimm$imm$fun(start, end, this); }
  _flowread$read(start, end) { return base$$List_1._flowread$read$fun(start, end, this); }
}
