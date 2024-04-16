package base;

//TODO: fix me using LList
import java.lang.reflect.Field;
import java.util.List;
public class FearlessMain {
  public static void main(String[] args) throws Throwable {
    try {_main(args);}
    catch(Throwable t) { t.printStackTrace(System.out); }
      _main(args);
    try {
    }  catch (StackOverflowError e) {
      System.err.println("Program crashed with Stack overflow");
      System.exit(1);
    }
    catch (Throwable t) {
      System.err.println("Program crashed with: "+t.getMessage());
      System.exit(1);
    }
  }
  public static void _main(String[] args) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
    //System.out.println("Command is \n"+List.of(args));
    String mainName = args[0];
    Class<?> clazz= Class.forName(mainName+"_0Impl");
    //TODO: what about generic mains?
    Field f= clazz.getField("$self");
    var myMain=(base.Main_0)f.get(null);
    // TODO: generate launch args again
    rt.NativeRuntime.println(myMain.$hash$imm(base.LList_1.$self).utf8());
  }
}
