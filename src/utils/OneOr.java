package utils;

import java.util.stream.Stream;

public class OneOr{
  public static <T> T of(String err, Stream<T> ts){
    return ts
      .reduce( (a,b)->{throw new RuntimeException(err);})
      .orElseGet(()->{throw new RuntimeException(err);});
  }
}
