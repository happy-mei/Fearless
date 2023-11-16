package pltRedex;

import ast.E;
import ast.T;
import id.Id;
import main.Main;
import parser.Parser;
import program.inference.InferBodies;
import utils.Push;
import utils.Streams;
import visitors.Visitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record RedexTermGenerator(ArrayList<String> tops, List<Id.GX<T>> inScopeGXs) implements Visitor<String> {
  public static void main(String[] args) throws IOException {
    Main.resetAll();
    var path = Path.of(args[0]);
    var code = Parser.codeFromPath(path);
    var p = Parser.parseAll(List.of(new Parser(path, code)));
    var inferred = InferBodies.inferAll(p);
    var tops = new ArrayList<String>();
    var visitor = new RedexTermGenerator(tops, List.of());
    inferred.ds().values().forEach(visitor::visitDec);
    var res = "(term ["+ String.join("\n", tops) +"])";
    System.out.println(res);
  }

  @Override public String visitX(E.X e) {
    return e.name();
  }
  @Override public String visitMCall(E.MCall e) {
    var recv = e.receiver().accept(this);
    var gens = "("+e.ts().stream().map(this::visitT).collect(Collectors.joining(" "))+")";
    var args = "("+e.es().stream().map(e_->e_.accept(this)).collect(Collectors.joining(" "))+")";
    return "("+recv+" "+visitMName(e.name())+" "+gens+" "+args+")";
  }
  @Override public String visitLambda(E.Lambda e) {
    // This is a simple version of generic parameter funneling which forwards all gens in scope
    // rather than just the ones in use.
    Id.DecId fresh = new Id.DecId("fakepkg."+Id.GX.fresh().name(), inScopeGXs.size());
    var top = new T.Dec(fresh, inScopeGXs.stream().distinct().toList(), Map.of(), e, e.pos());
    return visitDec(top);
  }

  public String visitDec(T.Dec top) {
    var e = top.lambda();
    var visitor = new RedexTermGenerator(tops, Push.of(inScopeGXs, top.gxs()));
    var its = e.its().stream().filter(it->!it.name().equals(top.name())).map(visitor::visitIT).collect(Collectors.joining(" "));
    var meths = e.meths().stream().map(visitor::visitMeth).collect(Collectors.joining(" "));
    var res = "("+visitor.visitIT(top.toIT())+" : ("+its+" {\\' "+e.selfName().replace("$", "N")+" "+meths+"}))";
    tops.add(res);
    return res;
  }
  public String visitMeth(E.Meth m) {
    var params = "("+Streams.zip(m.xs(), m.sig().ts()).map((x,t)->"("+x+" "+visitT(t)+")").collect(Collectors.joining(" "))+")";
    var gens = "("+m.sig().gens().stream().map(this::visitGX).collect(Collectors.joining(" "))+")";
    var sig = "("+visitMName(m.name())+" "+gens+" "+params+" : "+visitT(m.sig().ret())+")";
    if (m.isAbs()) { return "("+sig+" \\,)"; }
    var body = m.body().orElseThrow().accept(new RedexTermGenerator(tops, Push.of(inScopeGXs, m.sig().gens())));
    return "("+sig+" -> "+body+" \\,)";
  }

  public String visitT(T t) {
    // Intentionally ignoring MDF in this version
    return t.match(this::visitGX, this::visitIT);
  }
  public String visitIT(Id.IT<T> it) {
    var gens = "("+it.ts().stream().map(this::visitT).collect(Collectors.joining(" "))+")";
    return "("+it.name().shortName()+" "+gens+")";
  }
  public String visitGX(Id.GX<T> gx) {
    return gx.name().replace("/","Dth").replace("$", "N");
  }

  public String visitMName(Id.MethName name) {
    if (name.name().charAt(0) == '.') {
      return "(\\. "+name.name().substring(1)+")";
    }
    return name.name();
  }
}
