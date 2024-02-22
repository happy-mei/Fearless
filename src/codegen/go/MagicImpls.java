package codegen.go;

import codegen.MIR;
import failure.Fail;
import id.Id;
import magic.MagicCallable;
import magic.MagicTrait;
import utils.Bug;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static magic.MagicImpls.getLiteral;

public record MagicImpls(PackageCodegen gen, ast.Program p) implements magic.MagicImpls<MagicImpls.Res> {
  public record Res(String output, Set<String> imports) {
    public Res(String output) { this(output, Set.of()); }
  }
  @Override public MagicTrait<MIR.E, Res> int_(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public Res instantiate() {
        var lit = getLiteral(p, name);
        try {
          return new Res(lit
            .map(lambdaName->"int64("+Long.parseLong(lambdaName.replace("_", ""), 10)+")")
            .orElseGet(()->"int64("+e.accept(gen, true)+")"));
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Int");
        }
      }

      @Override public Optional<Res> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(_call(m, args));
      }
      private Res _call(Id.MethName m, List<? extends MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".uint", 0))) {
          return new Res("uint64("+instantiate().output()+")");
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return new Res("float64("+instantiate().output()+")");
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return new Res("strconv.FormatInt("+instantiate().output()+", 10)", Set.of("strconv"));
        }
        if (m.equals(new Id.MethName("+", 1))) { return new Res(instantiate().output()+" + "+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("-", 1))) { return new Res(instantiate().output()+" - "+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("*", 1))) { return new Res(instantiate().output()+"*"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("/", 1))) { return new Res(instantiate().output()+"/"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("%", 1))) { return new Res(instantiate().output()+"%"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("**", 1))) { return new Res(String.format("baseφrtφPow(%s, %s)", instantiate().output(), args.getFirst().accept(gen, true)), Set.of()); }
        if (m.equals(new Id.MethName(".abs", 0))) { return new Res("baseφrtφAbs("+instantiate().output()+")", Set.of("rt/rt/baseφrt")); }
        if (m.equals(new Id.MethName(">>", 1))) { return new Res(instantiate().output()+">>"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("<<", 1))) { return new Res(instantiate().output()+"<<"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("^", 1))) { return new Res(instantiate().output()+"^"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("&", 1))) { return new Res(instantiate().output()+"&"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("|", 1))) { return new Res(instantiate().output()+"|"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(">", 1))) { return new Res("baseφrtφConvertBool("+instantiate().output()+">"+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("<", 1))) { new Res("baseφrtφConvertBool("+instantiate().output()+"<"+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName(">=", 1))) { new Res("baseφrtφConvertBool("+instantiate().output()+">="+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("<=", 1))) { new Res("baseφrtφConvertBool("+instantiate().output()+"<="+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("==", 1))) { new Res("baseφrtφConvertBool("+instantiate().output()+"=="+args.getFirst().accept(gen, true)+")", Set.of()); }
        throw Bug.unreachable();
      }
    };
  }

  // TODO: golang computes constant arithmetic at compile-time and will throw for invalid UInts in that case, which is annoyingly different behaviour than Java
  @Override public MagicTrait<MIR.E, Res> uint(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public Res instantiate() {
        var lit = getLiteral(p, name);
        try {
          return new Res(lit
            .map(lambdaName->"uint64("+Long.parseUnsignedLong(lambdaName.substring(0, lambdaName.length()-1).replace("_", ""), 10)+")")
            .orElseGet(()->"uint64("+e.accept(gen, true)+")"));
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "UInt");
        }
      }

      @Override public Optional<Res> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(_call(m, args));
      }
      private Res _call(Id.MethName m, List<? extends MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".int", 0))) {
          return new Res("uint64("+instantiate().output()+")");
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return new Res("float64("+instantiate().output()+")");
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return new Res("strconv.FormatUint("+instantiate().output()+", 10)", Set.of("strconv"));
        }
        if (m.equals(new Id.MethName("+", 1))) { return new Res(instantiate().output()+" + "+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("-", 1))) { return new Res(instantiate().output()+" - "+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("*", 1))) { return new Res(instantiate().output()+"*"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("/", 1))) { return new Res(instantiate().output()+"/"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("%", 1))) { return new Res(instantiate().output()+"%"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("**", 1))) { return new Res(String.format("baseφrtφPow(%s, %s)", instantiate().output(), args.getFirst().accept(gen, true)), Set.of()); }
        if (m.equals(new Id.MethName(".abs", 0))) { return new Res("baseφrtφAbs("+instantiate().output()+")", Set.of("rt/rt/baseφrt")); }
        if (m.equals(new Id.MethName(">>", 1))) { return new Res(instantiate().output()+">>"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("<<", 1))) { return new Res(instantiate().output()+"<<"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("^", 1))) { return new Res(instantiate().output()+"^"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("&", 1))) { return new Res(instantiate().output()+"&"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("|", 1))) { return new Res(instantiate().output()+"|"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(">", 1))) { return new Res("baseφrtφConvertBool("+instantiate().output()+">"+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("<", 1))) { return new Res("baseφrtφConvertBool("+instantiate().output()+"<"+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName(">=", 1))) { return new Res("baseφrtφConvertBool("+instantiate().output()+">="+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("<=", 1))) { return new Res("baseφrtφConvertBool("+instantiate().output()+"<="+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("==", 1))) { return new Res("baseφrtφConvertBool("+instantiate().output()+"=="+args.getFirst().accept(gen, true)+")", Set.of()); }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<MIR.E, Res> float_(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public Res instantiate() {
        var lit = getLiteral(p, name);
        try {
          return new Res(lit
            .map(lambdaName->"float64("+Double.parseDouble(lambdaName.replace("_", ""))+")")
            .orElseGet(()->"float64("+e.accept(gen, true)+")"));
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Float");
        }
      }

      @Override public Optional<Res> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        throw Bug.todo();
      }
    };
  }

  @Override public MagicTrait<MIR.E, Res> str(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public Res instantiate() {
        var lit = getLiteral(p, name);
        return new Res(lit.orElseGet(()->"string("+e.accept(gen, true)+")"));
      }

      @Override
      public Optional<Res> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName(".size", 0))) { return Optional.of(new Res("uint64(len("+Optional.of(instantiate().output()+"))"))); }
        if (m.equals(new Id.MethName(".isEmpty", 0))) { return Optional.of(new Res("baseφrtφConvertBool(len("+instantiate().output()+") == 0)", Set.of())); }
        if (m.equals(new Id.MethName(".str", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName(".toImm", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName("+", 1))) { return Optional.of(new Res("("+instantiate().output()+"+"+args.getFirst().accept(gen, true)+")")); }
        if (m.equals(new Id.MethName("==", 1))) {
          return Optional.of(new Res("baseφrtφConvertBool("+instantiate().output()+" == "+args.getFirst().accept(gen, true)+")", Set.of()));
        }
        if (m.equals(new Id.MethName(".assertEq", 1))) {
//          return Optional.of("base.$95StrHelpers_0._$self.assertEq$imm$("+instantiate()+", "+args.getFirst().accept(gen, true)+")");
          throw Bug.todo(); // TODO
        }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<MIR.E, Res> debug(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, Res> refK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, Res> isoPodK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, Res> assert_(MIR.E e) {
    return null;
  }

  @Override public MagicCallable<MIR.E, Res> variantCall(MIR.E e) {
    return null;
  }
}
