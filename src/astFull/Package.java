package astFull;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import files.Pos;
import generated.FearlessParser.TopDecContext;
import main.Fail;
import utils.Bug;
import visitors.FullEAntlrVisitor;

public record Package(
    String name,
    List<T.Alias> as,
    List<TopDecContext> ds,
    List<Path> ps
    ){
  public Map<T.DecId,T.Dec> parse(){
    var res = new HashMap<T.DecId,T.Dec>();
    IntStream.range(0, this.ds().size()).forEach(i->this.acc(res, i));
    return Collections.unmodifiableMap(res);
  }
  private void acc(Map<T.DecId,T.Dec> acc,int i){
    Path pi = this.ps().get(i);
    TopDecContext di=this.ds().get(i);
    T.Dec dec=new FullEAntlrVisitor(pi,this::resolve).visitTopDec(di, this.name());
    T.DecId id=new T.DecId(dec.name(),dec.xs().size());

    var declPos = di.getStart();
    PosMap.add(dec, Pos.of(pi.toUri(), declPos.getLine(), declPos.getCharPositionInLine()));

    acc.put(id, dec);
  }
  Optional<T.IT> resolve(String base){
    return this.as.stream()
      .filter(a -> base.equals(a.to()))
      .findAny()
      .map(a -> a.from());
  }
  public static Package merge(List<T.Alias>global,List<Package>ps){
    assert checks(global,ps);
    // TODO: This gives a nicer error but is actually redundant because all top decls are aliases too!
//    topDecDisj(ps);
    var allAliases=mergeAlias(global,ps);
    aliasDisj(allAliases);
    var decls=ps.stream().flatMap(p->p.ds().stream()).toList();
    var paths=ps.stream().flatMap(p->p.ps().stream()).toList();
    return new Package(ps.get(0).name(),allAliases,decls,paths);
  }
  static List<T.Alias> mergeAlias(List<T.Alias>global, List<Package>ps){
    return Stream.concat(
        Stream.concat(
            global.stream(),
            ps.stream().flatMap(p->p.as().stream())
        ),
        ps.stream().flatMap(p->p.parse()
            .values()
            .stream()
            .map(d->{
              assert d.name().startsWith(p.name());
              var shortName = d.name().substring(p.name().length()+1);
              var alias = new T.Alias(new T.IT(d.name(), d.xs()), shortName);
              PosMap.add(alias, PosMap.get(d).orElseThrow(Bug::unreachable));
              return alias;
            })
        )
    ).toList();
  }
  static void aliasDisj(List<T.Alias> all){
    var seen = new HashSet<String>(all.size());
    for (var a : all) {
      var aliased = a.to();
      if (seen.add(aliased)) {continue;}
      var conflicts = all.stream()
          .filter(al->al.to().equals(aliased))
          .map(al->Fail.conflict(PosMap.getOrUnknown(al), a.toString()))
          .toList();
      throw Fail.conflictingAlias(aliased, conflicts);
    }
  }
  static void topDecDisj(List<Package>ps){
//    var decls = new HashMap<String, Set<T.DecId>>();
//    ps.forEach(p -> {
//      var pkgDecls = p.parse();
//      var ds = decls.getOrDefault(p.name(), new HashSet<>());
//      decls.put(p.name(), ds);
//    });
  }

  static boolean checks(List<T.Alias>global,List<Package>ps){
    assert !ps.isEmpty();
    var n=ps.get(0).name();
    assert ps.stream().allMatch(p->p.name().equals(n));    
    return true;
  }
}
