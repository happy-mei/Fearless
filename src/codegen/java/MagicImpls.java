package codegen.java;

import ast.T;
import codegen.MIR;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.MagicTrait;
import utils.Bug;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static magic.MagicImpls.getLiteral;

public record MagicImpls(JavaCodegen gen, ast.Program p) implements magic.MagicImpls<String> {
  @Override public MagicTrait<MIR.E,String> int_(MIR.E e) {
    var name = e.t().itOrThrow();
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }

      @Override public String instantiate() {
        var lit = getLiteral(p, name.name());
        try {
          return lit
            .map(lambdaName->Long.parseLong(lambdaName.replace("_", ""), 10)+"L")
            .orElseGet(()->"((long)"+e.accept(gen, true)+")");
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Int");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        return Optional.ofNullable(_call(m, args));
      }
      private String _call(Id.MethName m, List<MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".uint", 0))) {
          return instantiate(); // only different at type level
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return "("+"(double)"+instantiate()+")";
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "Long.toString("+instantiate()+")";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("/", 1))) { return instantiate()+"/"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("%", 1))) { return instantiate()+"%"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
          (switch (1) { default -> {
              long base = %s; long exp = %s; long res = base;
              if (exp == 0) { yield 1L; }
              for(; exp > 1; exp--) { res *= base; }
              yield res;
            }})
          """, instantiate(), args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return "Math.abs("+instantiate()+")"; }
        if (m.equals(new Id.MethName(">>", 1))) { return instantiate()+">>"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("<<", 1))) { return instantiate()+"<<"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("^", 1))) { return instantiate()+"^"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("&", 1))) { return instantiate()+"&"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("|", 1))) { return instantiate()+"|"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate()+">"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate()+"<"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate()+">="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate()+"<="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "("+instantiate()+"=="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> uint(MIR.E e) {
    var name = e.t().itOrThrow();
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }

      @Override public String instantiate() {
        var lit = getLiteral(p, name.name());
        try {
          return lit
            .map(lambdaName->Long.parseUnsignedLong(lambdaName.substring(0, lambdaName.length()-1).replace("_", ""), 10)+"L")
            .orElseGet(()->"((long)"+e.accept(gen, true)+")");
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "UInt");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        return Optional.of(_call(m, args));
      }
      private String _call(Id.MethName m, List<MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".int", 0))) {
          return instantiate(); // only different at type level
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return "("+"(double)"+instantiate()+")";
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "Long.toUnsignedString("+instantiate()+")";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("/", 1))) { return "Long.divideUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")"; }
        if (m.equals(new Id.MethName("%", 1))) { return "Long.remainderUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")"; }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
          (switch (1) { default -> {
              long base = %s; long exp = %s; long res = base;
              if (exp == 0) { yield 1L; }
              for(; exp > 1; exp--) { res *= base; }
              yield res;
            }})
          """, instantiate(), args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return instantiate(); } // no-op for unsigned
        if (m.equals(new Id.MethName(">>", 1))) { return instantiate()+">>"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("<<", 1))) { return instantiate()+"<<"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("^", 1))) { return instantiate()+"^"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("&", 1))) { return instantiate()+"&"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("|", 1))) { return instantiate()+"|"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(">", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")>0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")<0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")>=0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")<=0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")==0?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }
  @Override public MagicTrait<MIR.E,String> float_(MIR.E e) {
    var name = e.t().itOrThrow();
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }

      @Override public String instantiate() {
        var lit = getLiteral(p, name.name());
        try {
          return lit
            .map(lambdaName->Double.parseDouble(lambdaName.replace("_", ""))+"d")
            .orElseGet(()->"((double)"+e.accept(gen, true)+")");
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Float");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        return Optional.of(_call(m, args));
      }
      private String _call(Id.MethName m, List<MIR.E> args) {
        if (m.equals(new Id.MethName(".int", 0)) || m.equals(new Id.MethName(".uint", 0))) {
          return "("+"(long)"+instantiate()+")";
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate();
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "Double.toString("+instantiate()+")";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("/", 1))) { return instantiate()+"/"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("%", 1))) { return instantiate()+"%"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("Math.pow(%s, %s)", instantiate(), args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return "Math.abs("+instantiate()+")"; }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate()+">"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate()+"<"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate()+">="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate()+"<="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) {
          return "("+instantiate()+"=="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)";
        }
        //Float specifics
        if (m.equals(new Id.MethName(".round", 0))) { return "Math.round("+instantiate()+")"; }
        if (m.equals(new Id.MethName(".ceil", 0))) { return "Math.ceil("+instantiate()+")"; }
        if (m.equals(new Id.MethName(".floor", 0))) { return "Math.floor("+instantiate()+")"; }
        if (m.equals(new Id.MethName(".isNaN", 0))) { return "(Double.isNaN("+instantiate()+")?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(".isInfinite", 0))) { return "(Double.isInfinite("+instantiate()+")?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(".isPosInfinity", 0))) { return "("+instantiate()+" == Double.POSITIVE_INFINITY)?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(".isNegInfinity", 0))) { return "("+instantiate()+" == Double.NEGATIVE_INFINITY)?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> str(MIR.E e) {
    var name = e.t().itOrThrow();
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public String instantiate() {
        var lit = getLiteral(p, name.name());
        return lit.orElseGet(()->"((String)"+e.accept(gen, true)+")");
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName(".size", 0))) { return Optional.of(instantiate()+".length()"); }
        if (m.equals(new Id.MethName(".isEmpty", 0))) { return Optional.of("("+instantiate()+".isEmpty()?base.True_0._$self:base.False_0._$self)"); }
        if (m.equals(new Id.MethName(".str", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName(".toImm", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName("+", 1))) { return Optional.of("("+instantiate()+"+"+args.getFirst().accept(gen, true)+")"); }
        if (m.equals(new Id.MethName("==", 1))) {
          return Optional.of("("+instantiate()+".equals("+args.getFirst().accept(gen, true)+")?base.True_0._$self:base.False_0._$self)");
        }
        if (m.equals(new Id.MethName(".assertEq", 1))) {
          return Optional.of("base.$95StrHelpers_0._$self.assertEq$imm$("+instantiate()+", "+args.getFirst().accept(gen, true)+")");
        }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> debug(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() {
        return e.t().itOrThrow();
      }

      @Override public String instantiate() {
        return e.accept(gen, false);
      }

      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName("#", 1))) {
          var x = args.getFirst();
          return Optional.of(String.format("""
            (switch (1) { default -> {
              var x = %s;
              Object xObj = x;
              var strMethod = java.util.Arrays.stream(xObj.getClass().getMethods())
                .filter(meth->
                  (meth.getName().equals("str$read$") || meth.getName().equals("str$readOnly$"))
                  && meth.getReturnType().equals(String.class)
                  && meth.getParameterCount() == 0)
                .findAny();
              if (strMethod.isPresent()) {
                try {
                  System.out.println(strMethod.get().invoke(x));
                } catch(java.lang.IllegalAccessException | java.lang.reflect.InvocationTargetException err) {
                  System.out.println(x);
                }
              } else {
                System.out.println(x);
              }
              yield x;
            }})
            """, x.accept(gen, true)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> ref(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() {
        return e.t().itOrThrow();
      }

      @Override public String instantiate() {
        return e.accept(gen, false);
      }

      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        System.out.println(e);
        System.out.println(m);

//        if (m.equals(new Id.MethName(Optional.of(Mdf.imm), "#", 1))) {
//          var x = args.getFirst();
//          return Optional.of(String.format("""
//            new base.Ref_1(){
//              protected Object x = %s;
//              public Object get$mut$() { return this.x; }
//              public Object get$read$() { return this.x; }
//              public Object swap$mut$(Object x$) { var x1 = this.x; this.x = x$; return x1; }
//
//              public base.Void_0 set$mut$(Object x$);
//              public Object $60$45$mut$(Object f$);
//              public Object get$read$();
//              public Object get$mut$();
//              public base.Opt_1 getImm$read$();
//              public Object update$mut$(Object f$);
//              public Object $42$read$();
//              public Object $42$mut$();
//              public Object getImm$read$(Object f$);
//              public Object swap$mut$(Object x$);
//              public base.Void_0 $58$61$mut$(Object x$);
//            }
//            """, x.accept(gen, true)));
//        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> refK(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() {
        return e.t().itOrThrow();
      }

      @Override public String instantiate() {
        return e.accept(gen, false);
      }

      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName(Optional.of(Mdf.imm), "#", 1))) {
          var x = args.getFirst();
          return Optional.of(String.format("""
            new base.$95MagicRefImpl_1(){
              private Object x = %s;
              public Object get$mut$() { return this.x; }
              public Object get$read$() { return this.x; }
              public Object swap$mut$(Object x$) { var x1 = this.x; this.x = x$; return x1; }
            }
            """, x.accept(gen, true)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> isoPodK(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() {
        return e.t().itOrThrow();
      }

      @Override public String instantiate() {
        return e.accept(gen, false);
      }

      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName(Optional.of(Mdf.imm), "#", 1))) {
          var x = args.getFirst();
          return Optional.of(String.format("""
            new base$46caps.$95MagicIsoPodImpl_1(){
              private Object x = %s;
              private boolean isAlive = true;
              
              public base.Bool_0 isAlive$readOnly$() { return this.isAlive ? base.True_0._$self : base.False_0._$self; }
              public Object peek$readOnly$(Object f) { return this.isAlive ? ((base$46caps.IsoViewer_2)f).some$mut$(this.x) : ((base$46caps.IsoViewer_2)f).empty$mut$(); }
              public Object $33$mut$() {
                if (!this.isAlive) {
                  base.Error_0._$self.str$imm$("Cannot consume an empty IsoPod.");
                  return null;
                }
                this.isAlive = false;
                return this.x;
              }
              public base.Void_0 next$mut$(Object x) { this.isAlive = true; this.x = x; return new base.Void_0(){}; }
            }
            """, x.accept(gen, true)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> assert_(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() {
        return e.t().itOrThrow();
      }

      @Override public String instantiate() {
        return e.accept(gen, false);
      }

      @Override
      public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName("._fail", 0))) {
          return Optional.of("""
            (switch (1) { default -> {
              System.err.println("Assertion failed :(");
              System.exit(1);
              yield null;
            }})
            """);
        }
        if (m.equals(new Id.MethName("._fail", 1))) {
          return Optional.of(String.format("""
            (switch (1) { default -> {
              System.err.println(%s);
              System.exit(1);
              yield null;
            }})
            """, args.getFirst().accept(gen, true)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> errorK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> tryCatch(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> pipelineParallelSinkK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> objCap(Id.DecId magicTrait, MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> variantCall(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E,String> abort(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> magicAbort(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return e.t().itOrThrow(); }

      @Override public String instantiate() {
        return e.accept(gen, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName("!", 0))) {
          return Optional.of("""
            (switch (1) { default -> {
              System.err.println("No magic code was found at:\\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\\n")));
              System.exit(1);
              yield (Object)null;
            }})
            """);
        }
        return Optional.empty();
      }
    };
  }
}
