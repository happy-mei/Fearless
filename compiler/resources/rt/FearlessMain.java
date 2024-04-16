package base;

import java.lang.reflect.Field;
import java.util.List;
public class FearlessMain {
  public static void main(String[] args) {
    try {_main(args);}
    catch(Throwable t) { t.printStackTrace(System.out); }
    //TODO: somehow now the system hides all exceptions
   /*try {_main(args);}
      //System.out.println(entry.$hash$imm(FAux.LAUNCH_ARGS));
    catch (StackOverflowError e) {
      System.err.println(
        "Program crashed with Stack overflow");
      System.exit(1);
    }
    catch (Throwable t) {
      System.err.println(
        "Program crashed with: "+t.getLocalizedMessage());
      System.exit(1);
    }*/
  }
  public static void _main(String[] args) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
    //System.out.println("Command is \n"+List.of(args));
    String mainName= args[0];
    Class<?> clazz= Class.forName(mainName+"_0Impl");
    //TODO: what about generic mains?
    Field f= clazz.getField("$self");
    var myMain=(base.Main_0)f.get(null);
    myMain.$hash$imm(base.caps._System_0.$self);
  }
}
