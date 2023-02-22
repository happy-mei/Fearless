package codegen.java;

import ast.Program;
import ast.T;
import codegen.MIR;
import id.Id;
import magic.MagicTrait;
import utils.Bug;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MagicImpls(JavaCodegen gen, Program p) implements magic.MagicImpls<String> {
  @Override public MagicTrait<String> int_(MIR.Lambda e) {
    var name = new Id.IT<T>(e.freshName().name(), List.of());
    assert Character.isDigit(name.name().name().charAt(0)) : name;
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public MIR.Lambda instance() { return e; }
      @Override public String type() {
        return "Long";
      }
      @Override public String instantiate() {
        return name().name().name()+"L";
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        return Optional.of(_call(m, args, gamma));
      }
      private String _call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        // _NumInstance
        if (m.name().equals(".uint")) {
          return uint(e.withITs(List.of(new Id.IT<>(name().name().name()+"u", List.of())))).instantiate();
        }
        if (m.name().equals(".str")) {
          return str(e.withITs(List.of(new Id.IT<>("\""+name().name().name()+"\"", List.of())))).instantiate();
        }
        if (m.name().equals("+")) { return instantiate()+"+"+args.get(0).accept(gen); }
        if (m.name().equals("-")) { return instantiate()+"-"+args.get(0).accept(gen); }
        if (m.name().equals("*")) { return instantiate()+"*"+args.get(0).accept(gen); }
        if (m.name().equals("/")) { return instantiate()+"/"+args.get(0).accept(gen); }
        if (m.name().equals("%")) { return instantiate()+"%"+args.get(0).accept(gen); }
        if (m.name().equals("**")) {
          // TODO: implement this iteratively in fearless
          throw Bug.todo();
        }
        if (m.name().equals(">>")) { return instantiate()+">>"+args.get(0).accept(gen); }
        if (m.name().equals("<<")) { return instantiate()+"<<"+args.get(0).accept(gen); }
        if (m.name().equals("^")) { return instantiate()+"^"+args.get(0).accept(gen); }
        if (m.name().equals("&")) { return instantiate()+"&"+args.get(0).accept(gen); }
        if (m.name().equals("|")) { return instantiate()+"|"+args.get(0).accept(gen); }
        if (m.name().equals(">")) { return instantiate()+">"+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("<")) { return instantiate()+"<"+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals(">=")) { return instantiate()+">="+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("<=")) { return instantiate()+"<="+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("==")) { return instantiate()+"=="+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){}"; }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<String> uint(MIR.Lambda e) {
    var name = new Id.IT<T>(e.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public MIR.Lambda instance() { return e; }
      @Override public String type() {
        return "Long";
      }
      @Override public String instantiate() {
        if (Character.isDigit(name.name().name().charAt(0))) {
          var n = name().name().name();
          long val = Long.parseUnsignedLong(n.substring(0, n.length() - 1), 10);
          return val+"L";
        }
        // TODO: accept the MIR e here too and use that (i.e. the X)
        throw Bug.todo();
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        return Optional.of(_call(m, args, gamma));
      }
      private String _call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        // _NumInstance
        if (m.name().equals(".int")) {
          return uint(e.withITs(List.of(new Id.IT<>(name().name().name(), List.of())))).instantiate();
        }
        if (m.name().equals(".str")) {
          return str(e.withITs(List.of(new Id.IT<>("\""+name().name().name()+"\"", List.of())))).instantiate();
        }
        if (m.name().equals("+")) { return instantiate()+"+"+args.get(0).accept(gen); }
        if (m.name().equals("-")) { return instantiate()+"-"+args.get(0).accept(gen); }
        if (m.name().equals("*")) { return instantiate()+"*"+args.get(0).accept(gen); }
        if (m.name().equals("/")) { return "Long.divideUnsigned("+instantiate()+","+args.get(0).accept(gen)+")"; }
        if (m.name().equals("%")) { return "Long.remainderUnsigned("+instantiate()+","+args.get(0).accept(gen)+")"; }
        if (m.name().equals("**")) {
          // TODO: implement this iteratively in fearless
          throw Bug.todo();
        }
        if (m.name().equals(">>")) { return instantiate()+">>"+args.get(0).accept(gen); }
        if (m.name().equals("<<")) { return instantiate()+"<<"+args.get(0).accept(gen); }
        if (m.name().equals("^")) { return instantiate()+"^"+args.get(0).accept(gen); }
        if (m.name().equals("&")) { return instantiate()+"&"+args.get(0).accept(gen); }
        if (m.name().equals("|")) { return instantiate()+"|"+args.get(0).accept(gen); }
        if (m.name().equals(">")) { return "Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")>0?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("<")) { return "Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")<0?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals(">=")) { return "Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")>=0?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("<=")) { return "Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")<=0?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("==")) { return "Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")==0?new base.True_0(){}:new base.False_0(){}"; }
        throw Bug.unreachable();
      }
    };
  }
  @Override public MagicTrait<String> float_(MIR.Lambda e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<String> str(MIR.Lambda e) {
    var name = new Id.IT<T>(e.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public MIR.Lambda instance() { return e; }
      @Override public String type() {
        return "String";
      }
      @Override public String instantiate() {
        return name.name().name();
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName(".len", 0))) {
          return Optional.of(""+(name.name().name().length() - 2));
        }
        throw Bug.unreachable();
      }
    };
  }
  @Override public MagicTrait<String> refK(MIR.Lambda e) {
    throw Bug.todo();
  }
  @Override public MagicTrait<String> assert_(MIR.Lambda e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return e.t().itOrThrow(); }
      @Override public MIR.Lambda instance() { return e; }

      @Override public String type() {
        return "Assert_0";
      }
      @Override public String instantiate() {
        return gen.visitLambda(e, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName("._fail", 0))) {
          return Optional.of("""
            switch (1) { default -> {
              System.err.println("Assertion failed :(");
              System.exit(1);
              yield null;
            }}
            """);
        }
        if (m.equals(new Id.MethName("._fail", 1))) {
          return Optional.of(String.format("""
            switch (1) { default -> {
              System.err.println(%s);
              System.exit(1);
              yield null;
            }}
            """, args.get(0).accept(gen)));
        }
        return Optional.empty();
      }
    };
  }
}
