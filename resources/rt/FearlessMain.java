package rt;

import java.lang.reflect.Field;
import java.util.List;
import base.Main_0;
import base.caps.System_0;
import base.caps._System_0;
public class FearlessMain {
  public static void main(String[] args) {
    try {_main(args);}
    catch(Throwable t) { t.printStackTrace(System.out); }
    //TODO: somehow now the system hides all exceptions
  }
  public static void _main(String[] args) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
    //System.out.println("Command is \n"+List.of(args));
    String mainName= args[0];
    Class<?> clazz= Class.forName(mainName+"_0Impl");
    //TODO: what about generic mains?
    Field f= clazz.getField("$self");
    Main_0 myMain=(Main_0)f.get(null);
    //System_0 sys= (System_0)System_0.$self;//why this compiles??
    System_0 sys= (System_0)_System_0.$self;
    myMain.$hash$imm(sys);
  }
}
