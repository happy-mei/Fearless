package astFull;

import generated.FearlessParser.TopDecContext;
import main.Fail;
import utils.Range;
import utils.Streams;
import visitors.FullEAntlrVisitor;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

public record Package(
    String name,
    List<T.Alias> as,
    List<TopDecContext> ds,
    List<Path> ps
    ){
  public Map<T.DecId,T.Dec> parse(){
    var res = new HashMap<T.DecId,T.Dec>();
    IntStream.range(0, this.ds().size()).forEach(i->this.acc(res, i, false));
    return Collections.unmodifiableMap(res);
  }
  private Collection<T.Dec> shallowParse(){
    var res = new HashMap<T.DecId,T.Dec>();
    IntStream.range(0, this.ds().size()).forEach(i->this.acc(res, i, true));
    return res.values();
  }
  private void acc(Map<T.DecId,T.Dec> acc, int i, boolean shallow){
    Path pi = this.ps().get(i);
    TopDecContext di=this.ds().get(i);
    T.Dec dec=new FullEAntlrVisitor(pi,this::resolve).visitTopDec(di, this.name(), shallow);
    T.DecId id=new T.DecId(dec.name(),dec.xs().size());
    acc.put(id, dec);
  }
  Optional<T.IT> resolve(String base){
    if (!base.isEmpty() && Character.isDigit(base.charAt(0))) { return Optional.of(new T.IT(base, List.of())); }
    return this.as.stream()
      .filter(a -> base.equals(a.to()))
      .findAny()
      .map(a -> a.from());
  }
  public static Package merge(List<T.Alias>global,List<Package>ps) {
    assert checks(global, ps);
    // TODO: This gives a nicer error but is actually redundant because all top decls are aliases too!
    topDecDisj(ps);
    var allAliases = mergeAlias(global, ps);
    aliasDisj(allAliases);
    var decls = ps.stream().flatMap(p -> p.ds().stream()).toList();
    var paths = ps.stream().flatMap(p -> p.ps().stream()).toList();
    return new Package(ps.get(0).name(), allAliases, decls, paths);
  }
  static List<T.Alias> mergeAlias(List<T.Alias>global, List<Package>ps){
    return Streams.of(
      global.stream(),
      ps.stream().flatMap(p->p.as().stream()),
      ps.stream().flatMap(p->p.shallowParse().stream()
        .map(d->d.name())
        .map(n->{
          assert n.startsWith(p.name());
          var shortName = n.substring(p.name().length()+1);
          assert !shortName.contains(".");
          return new T.Alias(new T.IT(n,List.of()), shortName);
        })
        .distinct()
      )
    ).toList();
  }
  static void aliasDisj(List<T.Alias> all){
    var seen = new HashSet<String>(all.size());
    for(var a : all){
      var aliased = a.to();
      if(seen.add(aliased)){ continue; }
      var conflicts = all.stream()
        .filter(al->al.to().equals(aliased))
        .map(al->Fail.conflict(PosMap.getOrUnknown(al), a.toString()))
        .toList();
      throw Fail.conflictingAlias(aliased, conflicts);
    }
  }
  static void topDecDisj(List<Package>ps){
    var ds=ps.stream()
        .flatMap(p->p.ds().stream())
        .map(d->{
          int size=0;
          if(d.mGen()!=null && d.mGen().t()!=null){ size=d.mGen().t().size(); }
          return new T.DecId(d.fullCN().getText(),size);
          })
        .toList();
    var fns=ps.stream()
      .flatMap(p->Streams
        .zip(p.ds(),p.ps()).map((di,pi)->FullEAntlrVisitor.pos(pi, di))
         )
      .toList();
    assert ds.size()==fns.size();
    var uds=ds.stream().distinct().toList();
    if(uds.size()==ds.size()){ return; }
    var seen = new HashSet<T.DecId>(ds.size());
    for(var i:Range.of(ds)){
      T.DecId di=ds.get(i);
      if(seen.add(di)){ continue; }
      List<Fail.Conflict> conflicts = Streams.zip(ds,fns).filterMap((dj,fj)->{
        if(!dj.equals(di)){ return Optional.empty(); }
        return Optional.of(Fail.conflict(fj, di.toString()));
        }).toList();
      throw Fail.conflictingDecl(di, conflicts);
    }
  }

  static boolean checks(List<T.Alias>global,List<Package>ps){
    assert !ps.isEmpty();
    var n=ps.get(0).name();
    assert ps.stream().allMatch(p->p.name().equals(n));    
    return true;
  }
}