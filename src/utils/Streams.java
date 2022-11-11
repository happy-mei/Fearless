package utils;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Streams {
  @SafeVarargs
  public static <T> Stream<T> of(Stream<T>...ss){ return Stream.of(ss).flatMap(s->s); }
  
  public static <A,B> Zipper<A,B> zip(List<A> as, List<B> bs){
    assert as.size()==bs.size();
    return new Zipper<>(as,bs);
  }
  public record Zipper<A,B>(List<A> as, List<B> bs){
    public <R> Stream<R> map(BiFunction<A,B,R>f){
      return IntStream.range(0, as.size()).mapToObj(i->f.apply(as.get(i),bs.get(i)));
    }
    public <R> Stream<R> flatMap(BiFunction<A,B,Stream<R>>f){
      return IntStream.range(0, as.size()).boxed().flatMap(i->f.apply(as.get(i),bs.get(i)));
    }
    public <R> Stream<R> filterMap(BiFunction<A,B,Optional<R>>f){
      return IntStream.range(0, as.size())
        .mapToObj(i->f.apply(as.get(i),bs.get(i)))
        .filter(Optional::isPresent)
        .map(Optional::get);
    }
  }
}
