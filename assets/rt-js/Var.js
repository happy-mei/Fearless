export class Var {
  constructor(value) {
    this.value = value;
  }
  get$read() {
    return this.value;
  }
  get$mut() {
    return this.value;
  }
  swap$mut(newValue) {
    const old = this.value;
    this.value = newValue;
    return old;
  }
}
