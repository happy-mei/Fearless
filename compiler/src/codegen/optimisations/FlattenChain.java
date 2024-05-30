package codegen.optimisations;

import codegen.MIR;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Flatten a method call chain into a more easy to handle data-structure for easier analysis
 * and optimisations.
 * @param <Link> A link in the chain (i.e. BlockStmt for a Block# chain)
 * @param <Res> The data-structure that will hold the flattened chain
 */
interface FlattenChain<Link,Res> {
  enum FlattenStatus { INVALID, FLATTENED }

  Optional<Res> visitFluentCall(MIR.MCall call, List<Class<? extends Link>> validEndings, Optional<MIR.X> self);
  FlattenStatus flatten(MIR.E expr, Deque<Link> stmts, Optional<MIR.X> self);
}
