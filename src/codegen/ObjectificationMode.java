package codegen;

/** Mearless is not a generic language, so we need to turn some stuff into a top-type in certain cases:
 * Functions should be able to have real types and real return types.
 * Methods:
 *   - Return types can be real -> either is the same or it was objects (thus refinement)
 *   - Parameters can be real IF that parameter was not generic in any interface
 *   - Otherwise it must be object, and we define an overloaded version of the method with the real type,
 *     the objectified method calls the overloaded version with cast.
 * 'x' does not need casts
 * mcall needs the cast IF the return of the method was any
 * The 'new' should never need a cast
 * */
public enum ObjectificationMode {
  Keep
}


