package rt;

import java.lang.reflect.Field;

import base.Main_0;
import base.caps.System_0;
public class FearlessMain {
  public static void main(String[] args) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
    String mainName= args[0];
    Class<?> clazz= Class.forName(mainName);
    Field f= clazz.getField("$self");
    Main_0 myMain=(Main_0)f.get(null);
    System_0 sys= null;
    myMain.$hash$imm(sys);
  }
}
