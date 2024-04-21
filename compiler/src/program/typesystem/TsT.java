package program.typesystem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ast.T;
import id.Mdf;
import program.CM;

public record TsT(List<T> ts, T t, CM original){
  public TsT with(List<T> ts, T t){ return new TsT(ts, t, original); }
  public TsT renameMdfs(Map<Mdf, Mdf> replacements) {
    var ts = renameTsMdfs(replacements).ts();
    var t = renameTMdfs(replacements).t();
    return with(ts, t);
  }
  public TsT renameTsMdfs(Map<Mdf, Mdf> replacements) {
    List<T> ts = ts().stream()
      .map(ti->ti.withMdf(replacements.getOrDefault(ti.mdf(), ti.mdf())))
      .toList();
    return with(ts, t);
  }
  public TsT renameTMdfs(Map<Mdf, Mdf> replacements){
    T t= t().withMdf(replacements.getOrDefault(t().mdf(), t().mdf()));
    return with(ts, t);
  }
  public String toString(){
    return "("+ts.stream().map(T::toString)
      .collect(Collectors.joining(", "))+"): "+t;
  }
}