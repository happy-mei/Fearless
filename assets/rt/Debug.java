package rt;

import base.Void_0;

import java.util.regex.Pattern;

public final class Debug implements base.Debug_0 {
  public static final Debug $self = new Debug();

  @Override public Void_0 println$imm(Object x) {
    NativeRuntime.println(toStr(x).utf8());
    return Void_0.$self;
  }

  @Override public Str identify$imm(Object x) {
    return demangle(x);
  }

  @Override public Object $hash$imm(Object x) {
    NativeRuntime.println(toStr(x).utf8());
    return x;
  }

  private static Str toStr(Object x) {
    var strMethod = java.util.Arrays.stream(x.getClass().getMethods())
      .filter(meth->
        (meth.getName().equals("str$read") || meth.getName().equals("str$readH"))
          && meth.getReturnType().equals(rt.Str.class)
          && meth.getParameterCount() == 0)
      .findAny();
    if (strMethod.isPresent()) {
      try {
        return (Str)strMethod.get().invoke(x);
      } catch(java.lang.IllegalAccessException | java.lang.reflect.InvocationTargetException err) {
        return demangle(x);
      }
    } else {
      return demangle(x);
    }
  }

  private static final Pattern FEARLESS_TYPE_NAME = Pattern.compile("(.+)_(\\d+)(Impl)?(\\[.*])?$");
  private static Str demangle(Object x) {
    var strValue = x.toString();
    if (!strValue.matches(FEARLESS_TYPE_NAME.pattern())) {
      return Str.fromJavaStr(strValue);
    }

    var className = x.getClass().getName();
    var matcher = FEARLESS_TYPE_NAME.matcher(className);
    if (!matcher.matches()) {
      throw new RuntimeException("Cannot extract type name from "+strValue);
    }
    var typeName = matcher.group(1);
    var nGens = Integer.parseInt(matcher.group(2), 10);
    return Str.fromJavaStr(typeName+"/"+nGens);
  }
}
