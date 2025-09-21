import { rt$$IO } from './IO.js';

export class RealSystem {
  iso$mut$0() { return this; }
  self$mut$0() { return this; }

  io$mut$0() {
    return rt$$IO.$self;
  }

  // try$mut$0() {
  //   return new CapTry(); // implement like in your Java runtime
  // }
  //
  // rng$mut$0() {
  //   return Random.SeedGenerator.$self; // stub
  // }
}