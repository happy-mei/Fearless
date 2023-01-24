package typing;

import id.Mdf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestAdapt {
  @Test void comm1() {
    for (Mdf mdf1 : Mdf.values()) {
      for (Mdf mdf2 : Mdf.values()) {
        assertEquals(mdf1.adapt(mdf2), mdf2.adapt(mdf1), mdf1+", "+mdf2);
      }
    }
  }
}
