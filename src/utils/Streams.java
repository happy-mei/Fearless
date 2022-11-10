package utils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Streams {
  @SafeVarargs
  public static <T> Stream<T> of(Stream<T>...ss){ return Stream.of(ss).flatMap(s->s); }
  
  public static <A,B,R> Stream<R> zip(List<A> as, List<B> bs, BiFunction<A,B,R>f){
    assert as.size()==bs.size();
    return IntStream.range(0, as.size()).mapToObj(i->f.apply(as.get(i),bs.get(i)));
  }
}
