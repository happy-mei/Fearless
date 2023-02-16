package codegen.java;

import ast.E;
import ast.T;
import codegen.MIR;
import id.Id;
import magic.MagicTrait;
import utils.Bug;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MagicImpls(JavaCodegen gen) implements magic.MagicImpls<String> {
  @Override public MagicTrait<String> num(E.Lambda e) {
    var name = e.its().stream()
      .filter(it->Character.isDigit(it.name().name().charAt(0)))
      .findAny()
      .orElseThrow();
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public E.Lambda instance() { return e; }
      @Override public String instantiate() {
        return name().name().name();
      }
      @Override public String call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        // _NumInstance
        if (m.name().equals(".uInt")) {
          return unum(e.withITs(List.of(new Id.IT<>(name().name().name()+"u", List.of())))).instantiate();
        }
        if (m.name().equals(".str")) {
          return str(e.withITs(List.of(new Id.IT<>("\""+name().name().name()+"\"", List.of())))).instantiate();
        }
        if (m.name().equals("+")) { return instantiate()+"+"+args.get(0).accept(gen); }
        if (m.name().equals("-")) { return instantiate()+"-"+args.get(0).accept(gen); }
        if (m.name().equals("*")) { return instantiate()+"*"+args.get(0).accept(gen); }
        if (m.name().equals("/")) { return instantiate()+"/"+args.get(0).accept(gen); }
        if (m.name().equals("%")) { return instantiate()+"%"+args.get(0).accept(gen); }
        if (m.name().equals("**")) { return "Math.pow("+instantiate()+","+args.get(0).accept(gen)+")"; }
        if (m.name().equals(">>")) { return instantiate()+">>"+args.get(0).accept(gen); }
        if (m.name().equals("<<")) { return instantiate()+"<<"+args.get(0).accept(gen); }
        if (m.name().equals("^")) { return instantiate()+"^"+args.get(0).accept(gen); }
        if (m.name().equals("&")) { return instantiate()+"&"+args.get(0).accept(gen); }
        if (m.name().equals("|")) { return instantiate()+"|"+args.get(0).accept(gen); }
        if (m.name().equals(">")) { return instantiate()+">"+args.get(0).accept(gen)+"?new base.True(){}:new base.False();"; }
        if (m.name().equals("<")) { return instantiate()+"<"+args.get(0).accept(gen)+"?new base.True(){}:new base.False();"; }
        if (m.name().equals(">=")) { return instantiate()+">="+args.get(0).accept(gen)+"?new base.True(){}:new base.False();"; }
        if (m.name().equals("<=")) { return instantiate()+"<="+args.get(0).accept(gen)+"?new base.True(){}:new base.False();"; }
        if (m.name().equals("==")) { return instantiate()+"=="+args.get(0).accept(gen)+"?new base.True(){}:new base.False();"; }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<String> unum(E.Lambda e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<String> str(E.Lambda e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<String> refK(E.Lambda e) {
    throw Bug.todo();
  }
}
