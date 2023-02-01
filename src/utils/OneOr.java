package utils;

import main.CompileError;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class OneOr{
  public static <T> T of(String err, Stream<T> ts){
    return ts
      .reduce( (a,b)->{throw new RuntimeException(err);})
      .orElseThrow(()-> new RuntimeException(err));
  }
  public static <T> T of(Supplier<CompileError> err, Stream<T> ts){
    return ts
      .reduce( (a,b)->{throw new RuntimeException(err.get());})
      .orElseThrow(err);
  }
}
