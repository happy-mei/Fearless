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
    return d.pkg()+"."+getSimpleName(d);
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
  public boolean isDigit(int codepoint){
    //Character.isDigit is way too relaxed
    return codepoint >= '0' && codepoint <= '9';
  }
  public boolean isAlphabetic(int codepoint){
    return (codepoint >= 'A' && codepoint <= 'Z')
      || (codepoint >= 'a' && codepoint <= 'z');
    //return Character.isAlphabetic(codepoint);
    //Here instead I'm actually not sure what the best way is.
    //What do we want our identifiers to be?
  }
  public String getBase(String name) {
    String _name=name;
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.codePoints().mapToObj(c->{
      var base= isAlphabetic(c) || isDigit(c);
      if (base){ return Character.toString(c); }
      assert c != '.';
      var res= escape.get(c);
      assert res!=null
        :"not considered character ["+Character.toString(c)+"] in "+_name;
      return res;
    }).collect(Collectors.joining());
  }

  public String varName(String name){
    return Optional.ofNullable(keywordsMap.get(name))
      .orElse(name.replace("'","$")+"_m$");
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
  private static final Map<Integer, String> escape = Map.ofEntries(
    Map.entry((int)'_', "_"),
    Map.entry((int)'\'', "$apostrophe"),
    Map.entry((int)'+', "$plus"),
    Map.entry((int)'-', "$minus"),
    Map.entry((int)'*', "$asterisk"),
    Map.entry((int)'/', "$slash"),
    Map.entry((int)'\\', "$backslash"),
    Map.entry((int)'|', "$pipe"),
    Map.entry((int)'!', "$exclamation"),
    Map.entry((int)'@', "$at"),
    Map.entry((int)'#', "$hash"),
    Map.entry((int)'$', "$"),
    Map.entry((int)'%', "$percent"),
    Map.entry((int)'^', "$caret"),
    Map.entry((int)'&', "$ampersand"),
    Map.entry((int)'?', "$question"),
    Map.entry((int)'~', "$tilde"),
    Map.entry((int)'<', "$lt"),
    Map.entry((int)'>', "$gt"),
    Map.entry((int)'=', "$equals"),
    Map.entry((int)':', "$colon")
  );
}

