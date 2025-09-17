import { rt$$IO } from "./IO.js";
import { rt$$Str } from "./Str.js";
import { base$$Void_0 } from "../base/index.js";

export class rt$$Debug {
  static $self = new rt$$Debug();

  // Equivalent to Java println$imm
  println$imm(x) {
    rt$$IO.$self.printlnErr$mut(rt$$Debug.toStr(x).utf8());
    return base$$Void_0.$self;
  }

  // Equivalent to Java identify$imm
  identify$imm(x) {
    return rt$$Debug.demangle(x);
  }

  // Equivalent to Java $hash$imm
  $hash$imm(x) {
    rt$$IO.$self.printlnErr$mut(rt$$Debug.toStr(x).utf8());
    return x;
  }

  // Attempt to call x.str$read() or fallback to demangle
  static toStr(x) {
    if (x && typeof x === "object") {
      if (typeof x.str$read === "function") {
        try { return x.str$read(); }
        catch (_) { return rt$$Debug.demangle(x); }
      }
      if (typeof x.str$readH === "function") {
        try { return x.str$readH(); }
        catch (_) { return rt$$Debug.demangle(x); }
      }
    }
    return rt$$Debug.demangle(x);
  }

  // Demangle an object into a readable type string
  static demangle(x) {
    const strValue = x?.toString() ?? String(x);
    const match = strValue.match(rt$$Debug.FEARLESS_TYPE_NAME);
    if (!match) return rt$$Str.fromJsStr(strValue);

    const className = x?.constructor?.name ?? "<unknown>";
    const typeName = rt$$Debug.typeNameFromClassName(className);
    if (!typeName) throw new Error("Cannot extract type name from " + strValue);
    return rt$$Str.fromJsStr(typeName);
  }

  // Convert className like Foo_1Impl to Foo/1
  static typeNameFromClassName(className) {
    const match = className.match(rt$$Debug.FEARLESS_TYPE_NAME);
    if (!match) return null;
    const typeName = match[1];
    const nGens = parseInt(match[2], 10);
    return `${typeName}/${nGens}`;
  }

  // Optional: demangle a stack trace
  static demangleStackTrace(stackTrace) {
    return (stackTrace || []).map(frame => {
      return rt$$Debug.typeNameFromClassName(frame?.className) ?? `<runtime ${frame?.className}>`;
    }).join("\n");
  }
}

// Regex for Fearless type mangling: Foo_1Impl, Bar_2[], etc.
rt$$Debug.FEARLESS_TYPE_NAME = /^(.+)_(\d+)(Impl)?(\[.*])?$/;
