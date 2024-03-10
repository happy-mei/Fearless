package codegen.md;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import org.apache.commons.text.StringEscapeUtils;
import program.CM;
import utils.Bug;
import utils.Streams;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkdownDocgen {
  private final Program p;

  public MarkdownDocgen(Program p) { this.p = p; }
  public record PackageDoc(String pkgName, List<TraitDoc> traits) {}
  public record TraitDoc(String fileName, String markdown) {
    static TraitDoc fromPkg(String pkgName, String markdown) {
      return new TraitDoc(pkgName+".md", markdown);
    }
  }
  public List<PackageDoc> visitProgram() {
    Stream.concat(this.p.ds().values(), this.p.inlineDs().values())
      .map()
  }

  public TraitDoc visitTrait() {
    throw Bug.todo();
  }

  private static String fragment(Id.DecId d) {
    return URLEncoder.encode(d.name(), StandardCharsets.UTF_8).replace("+", "%20");
  }
  private static String fragment(String pkg, Id.DecId d, E.Meth m) {
    return URLEncoder.encode(d.name()+"_"+m.name(), StandardCharsets.UTF_8).replace("+", "%20");
  }
  private static String toURLPath(String pkg, String fragment) {
    return "../"+pkg+"/#"+fragment;
  }
}
