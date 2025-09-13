import { base$$Info_0 } from "../base/Info.js";
import { FearlessError} from "./FearlessError";

export const rt$$Error = {
  throwFearlessError(info) {
    if (!(info instanceof base$$Info_0)) {
      throw new TypeError("Expected base$$Info_0 instance for throwFearlessError");
    }
    throw new FearlessError(info);
  }
};
