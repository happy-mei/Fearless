package codegen.java;

import ast.E;
import ast.Program;
import ast.T;
import codegen.MIR;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import magic.MagicTrait;
import program.typesystem.EMethTypeSystem;
import utils.Bug;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static magic.MagicImpls.isLiteral;

public record MagicImpls(JavaCodegen gen, Program p, IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls) implements magic.MagicImpls<String> {
  @Override public MagicTrait<String> int_(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }

      @Override public String instantiate() {
        var lambdaName = name().name().name();
        try {
          return isLiteral(lambdaName) ? Long.parseLong(lambdaName.replace("_", ""))+"L" : "((long)"+e.accept(gen)+")";
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lambdaName, "Int");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        return Optional.ofNullable(_call(m, args, gamma));
      }
      private String _call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
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
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("/", 1))) { return instantiate()+"/"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("%", 1))) { return instantiate()+"%"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
          (switch (1) { default -> {
              long base = %s; long exp = %s; long res = base;
              if (exp == 0) { yield 1L; }
              for(; exp > 1; exp--) { res *= base; }
              yield res;
            }})
          """, instantiate(), args.getFirst().accept(gen)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return "Math.abs("+instantiate()+")"; }
        if (m.equals(new Id.MethName(">>", 1))) { return instantiate()+">>"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("<<", 1))) { return instantiate()+"<<"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("^", 1))) { return instantiate()+"^"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("&", 1))) { return instantiate()+"&"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("|", 1))) { return instantiate()+"|"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate()+">"+args.getFirst().accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate()+"<"+args.getFirst().accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate()+">="+args.getFirst().accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate()+"<="+args.getFirst().accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "("+instantiate()+"=="+args.getFirst().accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<String> uint(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }

      @Override public String instantiate() {
        var lambdaName = name().name().name();
        if (isLiteral(lambdaName)) {
          try {
            long l = Long.parseUnsignedLong(lambdaName.substring(0, lambdaName.length()-1).replace("_", ""));
            return l+"L";
          } catch (NumberFormatException err) {
            throw Fail.invalidNum(lambdaName, "UInt");
          }
        }
        return "((long)"+e.accept(gen)+")";
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        return Optional.of(_call(m, args, gamma));
      }
      private String _call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
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
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("/", 1))) { return "Long.divideUnsigned("+instantiate()+","+args.getFirst().accept(gen)+")"; }
        if (m.equals(new Id.MethName("%", 1))) { return "Long.remainderUnsigned("+instantiate()+","+args.getFirst().accept(gen)+")"; }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
          (switch (1) { default -> {
              long base = %s; long exp = %s; long res = base;
              if (exp == 0) { yield 1L; }
              for(; exp > 1; exp--) { res *= base; }
              yield res;
            }})
          """, instantiate(), args.getFirst().accept(gen)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return instantiate(); } // no-op for unsigned
        if (m.equals(new Id.MethName(">>", 1))) { return instantiate()+">>"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("<<", 1))) { return instantiate()+"<<"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("^", 1))) { return instantiate()+"^"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("&", 1))) { return instantiate()+"&"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("|", 1))) { return instantiate()+"|"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName(">", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen)+")>0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen)+")<0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen)+")>=0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen)+")<=0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen)+")==0?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }
  @Override public MagicTrait<String> float_(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }

      @Override public String instantiate() {
        var lambdaName = name().name().name();
        if (isLiteral(lambdaName)) {
          try {
            double l = Double.parseDouble(lambdaName.replace("_", ""));
            return l+"d";
          } catch (NumberFormatException err) {
            throw Fail.invalidNum(lambdaName, "Float");
          }
        }
        return "((double)"+e.accept(gen)+")";
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        return Optional.of(_call(m, args, gamma));
      }
      private String _call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName(".int", 0)) || m.equals(new Id.MethName(".uint", 0))) {
          return "("+"(long)"+instantiate()+")";
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate();
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "Double.toString("+instantiate()+")";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("/", 1))) { return instantiate()+"/"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("%", 1))) { return instantiate()+"%"+args.getFirst().accept(gen); }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("Math.pow(%s, %s)", instantiate(), args.getFirst().accept(gen)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return "Math.abs("+instantiate()+")"; }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate()+">"+args.getFirst().accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate()+"<"+args.getFirst().accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate()+">="+args.getFirst().accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate()+"<="+args.getFirst().accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) {
          return "("+instantiate()+"=="+args.getFirst().accept(gen)+"?base.True_0._$self:base.False_0._$self)";
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

  @Override public MagicTrait<String> str(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }

      @Override public String instantiate() {
        var lambdaName = name().name().name();
        return isLiteral(lambdaName) ? lambdaName : "((String)"+e.accept(gen)+")";
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName(".size", 0))) { return Optional.of(instantiate()+".length()"); }
        if (m.equals(new Id.MethName(".isEmpty", 0))) { return Optional.of("("+instantiate()+".isEmpty()?base.True_0._$self:base.False_0._$self)"); }
        if (m.equals(new Id.MethName(".str", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName(".toImm", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName("+", 1))) { return Optional.of("("+instantiate()+"+"+args.getFirst().accept(gen)+")"); }
        if (m.equals(new Id.MethName("==", 1))) {
          return Optional.of("("+instantiate()+".equals("+args.getFirst().accept(gen)+")?base.True_0._$self:base.False_0._$self)");
        }
        if (m.equals(new Id.MethName(".assertEq", 1))) {
          return Optional.of("base.$95StrHelpers_0._$self.assertEq$imm$("+instantiate()+", "+args.getFirst().accept(gen)+")");
        }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<String> debug(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }

      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
//        java.util.Arrays.stream(m.getClass().getDeclaredMethods())
//          .filter(meth->
//            meth.getName().equals("str$")
//              && meth.getReturnType().equals(String.class)
//              && meth.getParameterCount() == 0)
//          .findAny().ifPresent(msf -> msf.invoke());
        if (m.equals(new Id.MethName("#", 1))) {
          var x = args.getFirst();
          return Optional.of(String.format("""
            (switch (1) { default -> {
              var x = %s;
              Object xObj = x;
              var strMethod = java.util.Arrays.stream(xObj.getClass().getMethods())
                .filter(meth->
                  meth.getName().equals("str$read$")
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
            """, x.accept(gen)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> refK(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }

      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName(Optional.of(Mdf.imm), "#", 1))) {
          var x = args.getFirst();
          return Optional.of(String.format("""
            new base.Ref_1(){
              protected Object x = %s;
              public Object get$mut$() { return this.x; }
              public Object get$read$() { return this.x; }
              public Object swap$mut$(Object x$) { var x1 = this.x; this.x = x$; return x1; }
            }
            """, x.accept(gen)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> isoPodK(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }

      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName("#", 1))) {
          var x = args.getFirst();
          return Optional.of(String.format("""
            new base$46caps.IsoPod_1(){
              protected Object x = %s;
              protected boolean isAlive = true;
              
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
            """, x.accept(gen)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> assert_(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }

      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
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
            """, args.getFirst().accept(gen)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> abort(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }

      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName("!", 0))) {
          return Optional.of("""
            (switch (1) { default -> {
              System.err.println("Program aborted at:\\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\\n")));
              System.exit(1);
              yield (Object)null;
            }})
            """);
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> magicAbort(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }

      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName("!", 0))) {
          // todo: does this fail if used as an argument for something wanting like an int?
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

  @Override public MagicTrait<String> errorK(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }

      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName("!", 1))) {
          return Optional.of("""
            (switch (1) {
              default -> throw new FearlessError(%s);
              case 2 -> (Object)null;
            })
            """.formatted(args.getFirst().accept(gen)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> tryCatch(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }

      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName("#", 1))) {
          return Optional.of("""
            (switch (1) { default -> {
              try { yield base.Res_0._$self.ok$imm$(%s.$35$read$()); }
              catch(FearlessError _$err) { yield base.Res_0._$self.err$imm$(_$err.info); }
              catch(java.lang.ArithmeticException _$err) { yield base.Res_0._$self.err$imm$(base.FInfo_0._$self.str$imm$(_$err.getLocalizedMessage())); }
              catch(java.lang.StackOverflowError _$err) { yield base.Res_0._$self.err$imm$(base.FInfo_0._$self.str$imm$("Stack overflowed")); }
              catch(java.lang.VirtualMachineError _$err) { yield base.Res_0._$self.err$imm$(base.FInfo_0._$self.str$imm$(_$err.getLocalizedMessage())); }
            }})
            """.formatted(args.getFirst().accept(gen)));
        }
//        if (m.equals(new Id.MethName("#", 2))) {
//          return Optional.of("""
//            (switch (1) { default -> {
//              try { yield %s.$35$read$(); }
//              catch(FearlessError _$err) { yield %s.$35$mut$(_$err.info); }
//            }})
//            """.formatted(args.getFirst().accept(gen), args.get(1).accept(gen)));
//        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> pipelineParallelSinkK(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }

      @Override public String instantiate() {
        return "new rt.PipelineParallelFlow.WrappedSinkK()";
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> objCap(Id.DecId target, MIR.Lambda l, MIR e) {
    var _this = this;
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }

      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        ObjCapImpl impl = null;
        if (target == Magic.RootCap) { impl = (ctx, m1, args1, gamma1) -> null; }
        if (target == Magic.IO) { impl = io(); }
        if (target == Magic.FEnv) { impl = env(); }
        assert impl != null;

        var res = impl.call(_this, m, args, gamma);
        return Optional.ofNullable(res);
      }

      private ObjCapImpl io() {
        return (ctx, m, args, gamma) ->{
          if (m.equals(new Id.MethName(".print", 1))) {
            return String.format("""
              (switch (1) { default -> {
                System.out.print(%s);
                yield base.Void_0._$self;
              }})
              """, args.getFirst().accept(gen));
          }
          if (m.equals(new Id.MethName(".println", 1))) {
            return String.format("""
              (switch (1) { default -> {
                System.out.println(%s);
                yield base.Void_0._$self;
              }})
              """, args.getFirst().accept(gen));
          }
          if (m.equals(new Id.MethName(".printErr", 1))) {
            return String.format("""
              (switch (1) { default -> {
                System.err.print(%s);
                yield base.Void_0._$self;
              }})
              """, args.getFirst().accept(gen));
          }
          if (m.equals(new Id.MethName(".printlnErr", 1))) {
            return String.format("""
              (switch (1) { default -> {
                System.err.println(%s);
                yield base.Void_0._$self;
              }})
              """, args.getFirst().accept(gen));
          }
          return null;
        };
      }
      private ObjCapImpl env() {
        return (ctx, m, args, gamma) ->{
          if (m.equals(new Id.MethName("#", 1)) || m.equals(new Id.MethName(".io", 1))) {
            return """
              new base$46caps.Env_0(){
                public base.LList_1 launchArgs$mut$() { return FAux.LAUNCH_ARGS; }
              }
              """;
          }
          return null;
        };
      }
    };
  }

  @Override public MagicTrait<String> variantCall(MIR e) {
    return new MagicTrait<>() {
      @Override
      public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma, EnumSet<MIR.MCall.CallVariant> variants) {
        var call = (MIR.MCall) e;
        var recvT = call.recv().t().itOrThrow();
        if (recvT.name().equals(Magic.FlowK)) {
          if (m.equals(new Id.MethName(Optional.of(Mdf.imm), ".fromIter", 1))) {
            if (variants.contains(MIR.MCall.CallVariant.SafeMutSourceFlow)) {
              var newVariants = EnumSet.copyOf(variants);
              newVariants.remove(MIR.MCall.CallVariant.SafeMutSourceFlow);
              return Optional.of(gen.visitMCall(new MIR.MCall(
                new MIR.Lambda(Mdf.imm, new Id.DecId("base.flows._SafeSource", 0)),
                call.name(),
                call.args(),
                call.t(),
                call.mdf(),
                newVariants
              )));
            }
            if (variants.contains(MIR.MCall.CallVariant.Standard)) {
              return Optional.empty();
            }
          }
        }

        if (recvT.name().equals(Magic.FList)) {
          if (m.equals(new Id.MethName(Optional.of(Mdf.mut), ".flow", 0))) {
            if (variants.contains(MIR.MCall.CallVariant.SafeMutSourceFlow)) {
              var newVariants = EnumSet.copyOf(variants);
              newVariants.remove(MIR.MCall.CallVariant.SafeMutSourceFlow);
              return Optional.of(gen.visitMCall(new MIR.MCall(
                new MIR.Lambda(Mdf.imm, Magic.SafeFlowSource),
                new Id.MethName(Optional.of(Mdf.imm), ".fromList", 1),
                List.of(call.recv()),
                call.t(),
                Mdf.imm,
                newVariants
              )));
            }
            if (variants.contains(MIR.MCall.CallVariant.Standard)) {
              return Optional.empty();
            }
          }
        }

        if (recvT.name().equals(Magic.SafeFlowSource)) {
          if (variants.contains(MIR.MCall.CallVariant.PipelineParallelFlow)) {
            var op = gen.visitMCall(new MIR.MCall(
              new MIR.Lambda(Mdf.imm, Magic.SafeFlowSource),
              new Id.MethName(Optional.of(Mdf.imm), m.name()+"'", 1),
              args,
              new T(Mdf.mut, new Id.IT<>("base.flows.FlowOp", call.t().itOrThrow().ts())),
              Mdf.imm,
              EnumSet.of(MIR.MCall.CallVariant.Standard)
            ), false);
            // In the future I'll specifically call PipelineParFlow instead of SeqFlow, so magic can propagate
            // THE FUTURE IS NOW OLD MAN
            var parFlow = gen.visitMCall(new MIR.MCall(
              new MIR.Lambda(Mdf.imm, Magic.PipelineParallelFlowK),
              new Id.MethName(Optional.of(Mdf.imm), ".fromOp", 2),
              List.of(
                new MIR.MCall(
                  new MIR.Lambda(Mdf.imm, Magic.SafeFlowSource),
                  new Id.MethName(Optional.of(Mdf.imm), m.name()+"'", 1),
                  args,
                  new T(Mdf.mut, new Id.IT<>("base.flows.FlowOp", call.t().itOrThrow().ts())),
                  Mdf.imm,
                  EnumSet.of(MIR.MCall.CallVariant.Standard)
                ),
                new MIR.Lambda(Mdf.imm, new Id.DecId("base.Opt", 1)) // TODO: list size
              ),
              new T(Mdf.mut, new Id.IT<>("base.flows.Flow", call.t().itOrThrow().ts())),
              Mdf.imm,
              variants
            ));
            return Optional.of(parFlow);
//            return Optional.of("""
//              (switch (1) { default -> {
////                base$46flows.FlowOp_1 original = ...;
////                var subj = rt.PipelineParallelFlow.getSubject(123, null, (downstream, e) -> {
////                  System.out.println("A wizard is never early or late.");
////                }, () -> original.stop$mut$());
////                subj.ref().submit(new rt.FlowRuntime.Message.Data("yeet"));
////                subj.stop();
////                subj.signal().join();
////                yield base$46flows.$95PipelineParallelFlow_0._$self.fromOp$imm$(original, ...);
//                  yield %s;
//              }})
//              """.formatted(op, size, parFlow));
          }
        }

        System.err.println("Warning: No magic handler found for: "+e+"\nFalling back to Fearless implementation.");
        return Optional.empty();
      }

      @Override public Id.IT<T> name() {
        throw Bug.unreachable();
      }

      @Override public String instantiate() {
        throw Bug.unreachable();
      }
    };
  }
}
