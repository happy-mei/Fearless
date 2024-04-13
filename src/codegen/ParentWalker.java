package codegen;

import id.Mdf;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ParentWalker {
  /**
   * Walks through every trait in an inheritance chain, starting from the most concrete.
   */
  static Stream<MIR.TypeDef> of(MIR.Program p, MIR.TypeDef root) {
    var spliterator = Spliterators.spliteratorUnknownSize(new Iterator<MIR.TypeDef>() {
      private Deque<MIR.TypeDef> q = new ArrayDeque<>();
      { q.offer(root); }
      @Override public boolean hasNext() {
        return !q.isEmpty();
      }
      @Override public MIR.TypeDef next() {
        var d = q.poll();
        Objects.requireNonNull(d).impls().stream()
          .filter(ty->!ty.id().equals(d.name()))
          .map(ty->p.of(ty.id()))
          .forEach(q::offer);
        return d;
      }
    }, Spliterator.ORDERED & Spliterator.NONNULL);

    return StreamSupport.stream(spliterator, false);
  }

  // TODO: write tests to check how this works when multiple candidates at the same level exist
  static MIR.Sig leastSpecificSig(MIR.Program p, MIR.TypeDef root, FullMethId name) {
    return of(p, root)
      .flatMap(def->def.sigs().stream())
      .filter(sig->FullMethId.of(sig).equals(name))
      .toList()
      .getLast();
  }

  // TODO: write tests to check how this works when multiple candidates at the same level exist
  record FullMethId(Mdf mdf, String name, int args) {
    public static FullMethId of(MIR.Sig sig) {
      return new FullMethId(sig.mdf(), sig.name().name(), sig.name().num());
    }
  }
  /**
   * 
   * @param p
   * @param root
   * @return the most generic signature of all the super
   *  signatures for a give type (root)
   */
  static Map<FullMethId, MIR.Sig> leastSpecificSigs(MIR.Program p, MIR.TypeDef root) {
    return ParentWalker.of(p, root)
      .flatMap(def->def.sigs().stream())
      .collect(Collectors.toMap(FullMethId::of, sig->sig, (sigA, sigB)->sigB));
  }
}
