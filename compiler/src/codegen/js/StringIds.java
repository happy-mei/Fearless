package codegen.js;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import codegen.MIR;
import id.Id;
import id.Mdf;
import magic.LiteralKind;

final class StringIds {
  public static final StringIds $self = new StringIds();

  public Optional<String> getLiteral(ast.Program p, Id.DecId d) {
    return p.superDecIds(d).stream()
      .map(Id.DecId::name)
      .filter(LiteralKind::isLiteral)
      .findFirst();
  }

  public String getFullName(Id.DecId d) {
    return d.pkg().replace(".", "_") + "_" + getSimpleName(d);
  }

  public String getSimpleName(Id.DecId d) {
    return getBase(d.shortName()) + "_" + d.gen();
  }

  public String getFunName(MIR.FName name) {
    return getSimpleName(name.d()) + "$" + getMName(name.mdf(), name.m());
  }

  public String getMName(Mdf mdf, Id.MethName m) {
    return getBase(m.name()) + "$" + mdf;
  }

  public String getBase(String name) {
    if (name.startsWith(".")) name = name.substring(1);
    return name.codePoints()
      .mapToObj(c -> isValidJsChar(c) ? Character.toString(c) : "$" + c)
      .collect(Collectors.joining());
  }

  public String varName(String name) {
    return keywordsMap.getOrDefault(name,
      getBase(name).replace("'", "$apos"));
  }

  private boolean isValidJsChar(int c) {
    return (c >= 'a' && c <= 'z') ||
      (c >= 'A' && c <= 'Z') ||
      (c >= '0' && c <= '9') ||
      c == '_' || c == '$';
  }

  private static final Map<String, String> keywordsMap = Map.ofEntries(
    Map.entry("await", "$await"),
    Map.entry("break", "$break"),
    Map.entry("case", "$case"),
    Map.entry("catch", "$catch"),
    Map.entry("class", "$class"),
    Map.entry("const", "$const"),
    Map.entry("continue", "$continue"),
    Map.entry("debugger", "$debugger"),
    Map.entry("default", "$default"),
    Map.entry("delete", "$delete"),
    Map.entry("do", "$do"),
    Map.entry("else", "$else"),
    Map.entry("enum", "$enum"),
    Map.entry("export", "$export"),
    Map.entry("extends", "$extends"),
    Map.entry("false", "$false"),
    Map.entry("finally", "$finally"),
    Map.entry("for", "$for"),
    Map.entry("function", "$function"),
    Map.entry("if", "$if"),
    Map.entry("implements", "$implements"),
    Map.entry("import", "$import"),
    Map.entry("in", "$in"),
    Map.entry("instanceof", "$instanceof"),
    Map.entry("interface", "$interface"),
    Map.entry("let", "$let"),
    Map.entry("new", "$new"),
    Map.entry("null", "$null"),
    Map.entry("package", "$package"),
    Map.entry("private", "$private"),
    Map.entry("protected", "$protected"),
    Map.entry("public", "$public"),
    Map.entry("return", "$return"),
    Map.entry("super", "$super"),
    Map.entry("switch", "$switch"),
    Map.entry("this", "$this"),
    Map.entry("throw", "$throw"),
    Map.entry("true", "$true"),
    Map.entry("try", "$try"),
    Map.entry("typeof", "$typeof"),
    Map.entry("var", "$var"),
    Map.entry("void", "$void"),
    Map.entry("while", "$while"),
    Map.entry("with", "$with"),
    Map.entry("yield", "$yield")
  );
}

