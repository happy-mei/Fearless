import { rt$$Str } from "./Str.js";
import { base$$Infos_0 } from "../base/Infos_0.js";

/**
 * JS equivalent of rt.Try
 */
export class rt$$Try {
  static $self = new rt$$Try();

  $hash$imm(...args) {
    switch (args.length) {
      case 1: { // no-argument version
        const try$ = args[0];
        return (res) => {
          try {
            return res.ok$mut(try$.$hash$read());
          } catch (err) {
            if (err instanceof Error && err.isFearlessError) {
              return res.info$mut(err.info);
            } else {
              const msg = err.message || "Unknown error";
              return res.info$mut(base$$Infos_0.$self.msg$imm(rt$$Str.fromJsStr(msg)));
            }
          }
        };
      }
      case 2: { // one-argument version
        const [data, try$] = args;
        return (res) => {
          try {
            return res.ok$mut(try$.$hash$read(data));
          } catch (err) {
            if (err instanceof Error && err.isFearlessError) {
              return res.info$mut(err.info);
            } else {
              const msg = err.message || "Unknown error";
              return res.info$mut(base$$Infos_0.$self.msg$imm(rt$$Str.fromJsStr(msg)));
            }
          }
        };
      }
      default:
        throw new Error(`No overload for $hash$imm with ${args.length} arguments`);
    }
  }
}
