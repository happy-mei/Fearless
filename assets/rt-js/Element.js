import { rt$$Str } from "./Str.js";
import { base$$Void_0 } from "../base/index.js";

export class rt$$Element {
  constructor(domEl) {
    this._el = domEl;
  }

  // ----- text/value -----
  setTextContent$mut$1(text) {
    this._el.textContent = rt$$Str.toJsString(text);
    return base$$Void_0.$self;
  }

  get value$read$0() {
    return rt$$Str.fromJsStr(this._el.value ?? "");
  }

  setValue$mut$1(text) {
    this._el.value = rt$$Str.toJsString(text);
    return base$$Void_0.$self;
  }

  // ----- tree operations -----
  appendChild$mut$1(child) {
    this._el.appendChild(child._el);
    return base$$Void_0.$self;
  }

  remove$mut$0() {
    this._el.remove();
    return base$$Void_0.$self;
  }

  // ----- events -----
  addEventListener$mut$2(type, handler) {
    this._el.addEventListener(rt$$Str.toJsString(type), (e) => {
      // Call into Fearless block (assuming handler is a Block)
      handler.call$imm$1(e);
    });
    return base$$Void_0.$self;
  }

  // ----- misc -----
  focus$mut$0() {
    this._el.focus();
    return base$$Void_0.$self;
  }

  classList$read$0() {
    return this._el.classList;
  }

  toggleClass$mut$1(name) {
    this._el.classList.toggle(rt$$Str.toJsString(name));
    return base$$Void_0.$self;
  }
}
