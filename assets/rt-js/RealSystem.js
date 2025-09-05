import { IO } from './IO.js';

export class RealSystem {
  iso$mut() { return this; }
  self$mut() { return this; }

  io$mut() {
    return IO.$self;
  }

  // try$mut() {
  //   return new CapTry(); // implement like in your Java runtime
  // }
  //
  // rng$mut() {
  //   return Random.SeedGenerator.$self; // stub
  // }
}