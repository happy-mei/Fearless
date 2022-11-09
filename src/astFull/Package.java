package astFull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import generated.FearlessParser.TopDecContext;
import utils.Bug;
import visitors.FullEAntlrVisitor;

public record Package(
    String name,
    List<T.Alias> as,
    List<TopDecContext> ds,
    List<Path> ps
    ){
  public Package{
    
  }
  public Map<T.DecId,T.Dec> parse(){
    var res = new HashMap<T.DecId,T.Dec>();
    IntStream.range(0, this.ds().size()).forEach(i->this.acc(res, i));
    return Collections.unmodifiableMap(res);
  }
  private void acc(HashMap<T.DecId,T.Dec> acc,int i){
    Path pi = this.ps().get(i);
    TopDecContext di=this.ds().get(i);
    T.Dec dec=new FullEAntlrVisitor(pi,this::resolve).visitTopDec(di);
    T.DecId id=new T.DecId(dec.name(),dec.xs().size());
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
    var allAliases=mergeAlias(global,ps);
    topDecDisj(ps);
    var tops=ps.stream().flatMap(p->p.ds().stream()).toList();
    var paths=ps.stream().flatMap(p->p.ps().stream()).toList();
    return new Package(ps.get(0).name(),allAliases,tops,paths);
  }
  static List<T.Alias> mergeAlias(List<T.Alias>global,List<Package>ps){
    return Stream.concat(global.stream(),ps.stream().flatMap(p->p.as().stream())).toList();
  }
  static void aliasDisj(List<T.Alias>all){
    var ok=all.stream().map(a->a.to()).distinct().count()==all.size();
    if(ok){ return; }
    //TODO:
    throw Bug.todo();//.conflictingAlias(null, null)
  }
  static void topDecDisj(List<Package>ps){
    throw Bug.todo();
  }

  static boolean checks(List<T.Alias>global,List<Package>ps){
    assert !ps.isEmpty();
    var n=ps.get(0).name();
    assert ps.stream().allMatch(p->p.name().equals(n));    
    return true;
  }
}
