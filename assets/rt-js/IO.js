import { base$$Void_0 } from "../base/Void_0.js";

export class IO {
  static $self = new IO();

  printlnErr$mut(msg) {
    if (process && process.stdout) {
      process.stderr.write(msg.utf8() + "\n");
    } else {
      console.error(msg.utf8() + "\n");
    }
    return base$$Void_0.$self;
  }

  println$mut(msg) {
    if (process && process.stdout) {
      process.stdout.write(msg.utf8() + "\n");
    } else {
      console.log(msg.utf8() + "\n");
    }
    return base$$Void_0.$self;
  }

  print$mut(msg) {
    if (process && process.stdout) {
      process.stdout.write(msg.utf8());
    } else {
      console.log(msg.utf8());
    }
    return base$$Void_0.$self;
  }

  printErr$mut(msg) {
    if (process && process.stderr) {
      process.stderr.write(msg.utf8());
    } else {
      console.error(msg.utf8());
    }
    return base$$Void_0.$self;
  }

  accessR$mut(pathList) {
    // stub: implement like your rt.fs.ReadWritePath.readPath
    throw new Error("accessR$mut not implemented in JS runtime");
  }

  accessW$mut(pathList) {
    throw new Error("accessW$mut not implemented in JS runtime");
  }

  accessRW$mut(pathList) {
    throw new Error("accessRW$mut not implemented in JS runtime");
  }

  env$mut() {
    throw new Error("env$mut not implemented yet in JS runtime");
    // return Env.$self; // you’d define Env similar to IO
  }

  iso$mut() { return this; }
  self$mut() { return this; }

  // Equivalent of strListToPath
  static strListToPath(root, list) {
    throw new Error("strListToPath not implemented yet in JS runtime");
    // return list.reduce((acc, str) => acc + "/" + str.utf8(), root);
  }
}