package program.typesystem;

import id.Id;
import id.Mdf;

import java.util.Optional;
import java.util.Set;

public interface XBs {
  Set<Mdf> defaultBounds = Set.of(Mdf.read, Mdf.lent, Mdf.mut, Mdf.imm);

  default Set<Mdf> get(Id.GX<?> x) {
    return getO(x).orElse(defaultBounds);
  }
  default Optional<Set<Mdf>> getO(Id.GX<?> x) {
    return getO(x.name());
  }
  Optional<Set<Mdf>> getO(String s);
  static XBs empty(){ return x->Optional.empty(); }
  default XBs add(String x, Set<Mdf> bounds) {
    assert !bounds.contains(Mdf.mdf) && !bounds.contains(Mdf.recMdf);
    return xi->xi.equals(x) ? Optional.of(bounds) : this.getO(xi);
  }
}
