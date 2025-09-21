import { rt$$Str } from "./Str.js";
import { rt$$Element } from "./Element.js";

export class rt$$Document {
  static $self = new rt$$Document(document);

  constructor(domDoc) {
    this._doc = domDoc;
  }

  getElementById$imm$1(id) {
    const el = this._doc.getElementById(rt$$Str.toJsString(id));
    return new rt$$Element(el);
  }

  createElement$imm$1(tag) {
    const el = this._doc.createElement(rt$$Str.toJsString(tag));
    return new rt$$Element(el);
  }
}
