package utils;

import java.util.List;
import java.util.stream.Stream;

public class Push {
  public static <T> List<T> of(T t,List<T> ts){
    return Stream.concat(Stream.of(t),ts.stream()).toList();
  }
  public static <T> List<T> of(List<T> ts,T t){
    return Stream.concat(ts.stream(),Stream.of(t)).toList();
  }
}
