package files;

import java.net.URI;
import java.util.Optional;

public interface HasPos {
  Optional<Pos> pos();
  Pos unknown = Pos.of(URI.create("unknown"), 0, 0);
  default Pos posOrUnknown() {
    return pos().orElse(unknown);
  }
}
