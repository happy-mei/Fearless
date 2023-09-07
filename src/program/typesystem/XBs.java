package program.typesystem;

import id.Id;
import id.Mdf;

import java.util.Optional;
import java.util.Set;

public interface XBs {
  Set<Mdf> defaultBounds = Set.of(Mdf.read, Mdf.lent, Mdf.mut, Mdf.imm, Mdf.recMdf);

  default Set<Mdf> get(Id.GX<?> x) {
    return getO(x).orElse(defaultBounds);
  }
  Optional<Set<Mdf>> getO(Id.GX<?> s);
  static XBs empty(){ return x->Optional.empty(); }
  default XBs add(Id.GX<?> x, Set<Mdf> bounds) {
//    assert !bounds.contains(Mdf.mdf) && !bounds.contains(Mdf.recMdf);
    assert !bounds.contains(Mdf.mdf);
    return xi->xi.equals(x) ? Optional.of(bounds) : this.getO(x);
  }
}
