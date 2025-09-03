package codegen.js;

import codegen.FlowSelector;
import codegen.MIR;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import magic.MagicCallable;
import magic.MagicTrait;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.*;
import java.util.stream.*;

record JsNumOps(
  JsNumOps.NumOp onInt,
  JsNumOps.NumOp onNat,
  JsNumOps.NumOp onByte,
  JsNumOps.NumOp onFloat
) {

  public interface NumOp { String apply(String[] args); }

  private static final NumOp errOp = _ -> { throw new RuntimeException("Impossible case"); };

  private static final Map<Id.MethName, JsNumOps> numOps = new LinkedHashMap<>();

  private static Id.MethName m(String name, int arity) { return new Id.MethName(name, arity); }

  private static void put(String name, int arity, NumOp all) {
    singlePut(m(name, arity), new JsNumOps(all, all, all, all));
  }

  private static void put(String name, int arity, NumOp i, NumOp n, NumOp b, NumOp f) {
    singlePut(m(name, arity), new JsNumOps(i, n, b, f));
  }

  private static void singlePut(Id.MethName m, JsNumOps ops) {
    if (numOps.containsKey(m)) throw new RuntimeException("Duplicate op: " + m);
    numOps.put(m, ops);
  }

  public static JsNumOps emit(Id.MethName m) {
    return Optional.ofNullable(numOps.get(m))
      .orElseThrow(() -> new RuntimeException("Expected magic for: " + m));
  }

  public static String emitInt(Id.MethName m, String... args) {
    return emit(m).onInt().apply(args);
  }

  public static String emitNat(Id.MethName m, String... args) {
    return emit(m).onNat().apply(args);
  }

  public static String emitByte(Id.MethName m, String... args) {
    return emit(m).onByte().apply(args);
  }

  public static String emitFloat(Id.MethName m, String... args) {
    return emit(m).onFloat().apply(args);
  }

  public static String[] callArgs(MagicTrait<MIR.E,String> magic, List<? extends MIR.E> args, MIRVisitor<String> gen){
    String self = magic.instantiate().orElseThrow();
    Stream<String> rest = args.stream().map(a -> a.accept(gen, true));
    return Stream.concat(Stream.of(self), rest).toArray(String[]::new);
  }

  static {
    // Conversions - FIXED: Use actual argument references, not a[0]
    put(".int", 0,
      a -> a[0],                                      // int
      a -> a[0],                                      // nat
      a -> "(" + a[0] + " & 0xFF)",                   // byte
      a -> "Math.trunc(" + a[0] + ")"                 // float
    );

    put(".float", 0,
      a -> "(+" + a[0] + ")",                         // int
      a -> "(+" + a[0] + ")",                         // nat
      a -> "(+" + a[0] + ")",                         // byte
      a -> a[0]                                       // float
    );

    put(".str", 0,
      a -> "String(" + a[0] + ")",                    // int
      a -> "String(" + a[0] + ")",                    // nat
      a -> "String(" + a[0] + ")",                    // byte
      a -> "String(" + a[0] + ")"                     // float
    );

    // Arithmetic - FIXED: Use proper argument references
    put("+", 1,
      a -> "(" + a[0] + " + " + a[1] + ")",           // int
      a -> "(" + a[0] + " + " + a[1] + ")",           // nat
      a -> "((" + a[0] + " + " + a[1] + ") & 0xFF)",  // byte
      a -> "(" + a[0] + " + " + a[1] + ")"            // float
    );

    put("-", 1,
      a -> "(" + a[0] + " - " + a[1] + ")",           // int
      a -> "(" + a[0] + " - " + a[1] + ")",           // nat
      a -> "((" + a[0] + " - " + a[1] + ") & 0xFF)",  // byte
      a -> "(" + a[0] + " - " + a[1] + ")"            // float
    );

    put("*", 1,
      a -> "(" + a[0] + " * " + a[1] + ")",           // int
      a -> "(" + a[0] + " * " + a[1] + ")",           // nat
      a -> "((" + a[0] + " * " + a[1] + ") & 0xFF)",  // byte
      a -> "(" + a[0] + " * " + a[1] + ")"            // float
    );

    put("/", 1,
      a -> "Math.trunc(" + a[0] + " / " + a[1] + ")", // int
      a -> "Math.trunc(" + a[0] + " / " + a[1] + ")", // nat
      a -> "Math.trunc(" + a[0] + " / " + a[1] + ")", // byte
      a -> "(" + a[0] + " / " + a[1] + ")"            // float
    );

    put("%", 1,
      a -> "(" + a[0] + " % " + a[1] + ")",           // int
      a -> "(" + a[0] + " % " + a[1] + ")",           // nat
      a -> "(" + a[0] + " % " + a[1] + ")",           // byte
      a -> "(" + a[0] + " % " + a[1] + ")"            // float
    );

    put("**", 1,
      a -> "Math.pow(" + a[0] + ", " + a[1] + ")",    // int
      a -> "Math.pow(" + a[0] + ", " + a[1] + ")",    // nat
      a -> "Math.pow(" + a[0] + ", " + a[1] + ")",    // byte
      a -> "Math.pow(" + a[0] + ", " + a[1] + ")"     // float
    );

    // Comparisons
    put(">", 1,
      a -> "(" + a[0] + " > " + a[1] + ")",           // int
      a -> "(" + a[0] + " > " + a[1] + ")",           // nat
      a -> "(" + a[0] + " > " + a[1] + ")",           // byte
      a -> "(" + a[0] + " > " + a[1] + ")"            // float
    );

    put("<", 1,
      a -> "(" + a[0] + " < " + a[1] + ")",           // int
      a -> "(" + a[0] + " < " + a[1] + ")",           // nat
      a -> "(" + a[0] + " < " + a[1] + ")",           // byte
      a -> "(" + a[0] + " < " + a[1] + ")"            // float
    );

    put(">=", 1,
      a -> "(" + a[0] + " >= " + a[1] + ")",          // int
      a -> "(" + a[0] + " >= " + a[1] + ")",          // nat
      a -> "(" + a[0] + " >= " + a[1] + ")",          // byte
      a -> "(" + a[0] + " >= " + a[1] + ")"           // float
    );

    put("<=", 1,
      a -> "(" + a[0] + " <= " + a[1] + ")",          // int
      a -> "(" + a[0] + " <= " + a[1] + ")",          // nat
      a -> "(" + a[0] + " <= " + a[1] + ")",          // byte
      a -> "(" + a[0] + " <= " + a[1] + ")"           // float
    );

    put("==", 1,
      a -> "(" + a[0] + " === " + a[1] + ")",         // int
      a -> "(" + a[0] + " === " + a[1] + ")",         // nat
      a -> "(" + a[0] + " === " + a[1] + ")",         // byte
      a -> "(" + a[0] + " === " + a[1] + ")"          // float
    );

    put("!=", 1,
      a -> "(" + a[0] + " !== " + a[1] + ")",         // int
      a -> "(" + a[0] + " !== " + a[1] + ")",         // nat
      a -> "(" + a[0] + " !== " + a[1] + ")",         // byte
      a -> "(" + a[0] + " !== " + a[1] + ")"          // float
    );

    // Bitwise operations (only for int, nat, byte)
    put(".bitwiseAnd", 1,
      a -> "(" + a[0] + " & " + a[1] + ")",           // int
      a -> "(" + a[0] + " & " + a[1] + ")",           // nat
      a -> "((" + a[0] + " & " + a[1] + ") & 0xFF)",  // byte
      errOp                                           // float
    );
  }
}

public record JsMagicImpls(MIRVisitor<String> gen, ast.Program p) implements magic.MagicImpls<String> {
  @Override public MagicTrait<MIR.E, String> int_(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        var name = e.t().name().orElseThrow();
        var lit = magic.Magic.getLiteral(p, name);
        try {
          return lit
            .map(lambdaName -> lambdaName.startsWith("+") ? lambdaName.substring(1) : lambdaName)
            .map(lambdaName -> {
              long value = Long.parseLong(lambdaName.replace("_", ""), 10);
              return String.valueOf(value);
            })
            .orElseGet(() -> "(" + e.accept(gen, true) + ")")
            .describeConstable();
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Int");
        }
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(JsNumOps.emitInt(m, JsNumOps.callArgs(this, args, gen)));
      }
    };
  }

  @Override
  public MagicTrait<MIR.E, String> nat(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        var name = e.t().name().orElseThrow();
        var lit = magic.Magic.getLiteral(p, name);
        try {
          return lit
            .map(lambdaName -> {
              long value = Long.parseUnsignedLong(lambdaName.replace("_", ""), 10);
              return String.valueOf(value);
            })
            .orElseGet(() -> "(" + e.accept(gen, true) + " >>> 0)")
            .describeConstable();
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Nat");
        }
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args,
                                   EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(JsNumOps.emitNat(m, JsNumOps.callArgs(this, args, gen)));
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> float_(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        var name = e.t().name().orElseThrow();
        var lit = magic.Magic.getLiteral(p, name);
        try {
          return lit
            .map(lambdaName -> {
              // Parse double and output JS number
              double value = Double.parseDouble(lambdaName.replace("_", ""));
              return String.valueOf(value);
            })
            .orElseGet(() -> "parseFloat(" + e.accept(gen, true) + ")")
            .describeConstable();
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Float");
        }
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args,
                                   EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(JsNumOps.emitFloat(m, JsNumOps.callArgs(this, args, gen)));
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> byte_(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        var name = e.t().name().orElseThrow();
        var lit = magic.Magic.getLiteral(p, name);
        try {
          return lit
            .map(lambdaName -> {
              // Parse as unsigned byte (0-255)
              int value = Integer.parseUnsignedInt(lambdaName.replace("_", ""), 10);
              return String.valueOf(value & 0xFF); // Ensure byte range
            })
            .orElseGet(() -> "(" + e.accept(gen, true) + " & 0xFF)") // Byte masking
            .describeConstable();
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Byte");
        }
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args,
                                   EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(JsNumOps.emitByte(m, JsNumOps.callArgs(this, args, gen)));
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> str(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        throw Bug.unreachable();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> asciiStr(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, String> debug(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, String> refK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, String> isoPodK(MIR.E e) {
    return null;
  }

  @Override
  public MagicTrait<MIR.E, String> assert_(MIR.E e) {
    return new MagicTrait<>() {
      @Override
      public Optional<String> instantiate() {  return Optional.empty(); }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args,
                                   EnumSet<MIR.MCall.CallVariant> variants,
                                   MIR.MT expectedT) {
//        boolean isTestMode = ((JsCodegen)gen).testMode;  // Access the flag
        if (m.equals(new Id.MethName("._fail", 0))) {
          return Optional.of("""
            (function() {
             const err = new Error("Assertion failed");
             console.error(err);
             throw err;
            })()
            """);
        }
        if (m.equals(new Id.MethName("._fail", 1))) {
          return Optional.of(String.format("""
            (function() {
             const err = new Error(%s);
             console.error(err);
             throw err;
            })()
            """, args.get(0).accept(gen, true)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> cheapHash(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, String> regexK(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        // regex literals are not "instantiated" at CreateObj time in our design;
        // they are produced when a specific method (here '#') is called.
        return Optional.empty();
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        // In the Java backend the regex creation method is identified as
        // Optional.of(Mdf.imm), "#", 1  — match that same signature here.
        if (m.equals(new Id.MethName(Optional.of(Mdf.imm), "#", 1))) {
          // Use the runtime Regex wrapper (keeps parity with Java runtime), passing
          // the argument expression already codegen'd by the generator.
          return Optional.of("new rt.Regex(" + args.get(0).accept(gen, true) + ")");
//          // Alternatively, if you want native JS RegExp, use:
//           return Optional.of("new RegExp(" + args.get(0).accept(gen, true) + ")");
        }

        // Fallback to default behavior
        return MagicTrait.super.call(m, args, variants, expectedT);
      }
    };
  }

  @Override public MagicCallable<MIR.E,String> variantCall(MIR.E e) {
    return (m, args, variants, expectedT) -> {
      var call = (MIR.MCall) e;
      assert call.variant() == variants; // same check as Java version

      var parallelConstr = FlowSelector.bestParallelConstr(call);

      if (isMagic(Magic.FlowK, call.recv())) {
        if (m.name().equals("#") || m.name().equals(".ofIso")) {
          var listKCall = new MIR.MCall(
            new MIR.CreateObj(Mdf.imm, Magic.ListK),
            new Id.MethName(Optional.of(Mdf.imm), "#", call.args().size()),
            call.args(),
            new MIR.MT.Plain(Mdf.mut, Magic.FList),
            Mdf.imm,
            EnumSet.of(MIR.MCall.CallVariant.Standard)
          );
          var listFlowCall = new MIR.MCall(
            listKCall,
            new Id.MethName(Optional.of(Mdf.mut), ".flow", 0),
            List.of(),
            call.t(),
            Mdf.mut,
            variants
          );
          return Optional.of(gen.visitMCall(listFlowCall, true));
        }
        if (m.name().equals(".range") || m.name().equals(".ofIsos")) {
          assert parallelConstr.isPresent();
          return Optional.of(
            "rt.flows.FlowCreator.fromFlow(" +
              gen.visitCreateObj(new MIR.CreateObj(Mdf.imm, Magic.DataParallelFlowK), true) +
              ", " +
              call.withVariants(EnumSet.of(MIR.MCall.CallVariant.Standard)).accept(gen, true) +
              ")"
          );
        }
      }

      if (variants.contains(MIR.MCall.CallVariant.SafeMutSourceFlow)) {
        if (isMagic(Magic.FList, call.recv())) {
          var newVariants = EnumSet.copyOf(variants);
          newVariants.remove(MIR.MCall.CallVariant.SafeMutSourceFlow);
          return Optional.of(gen.visitMCall(new MIR.MCall(
            new MIR.CreateObj(Mdf.imm, Magic.SafeFlowSource),
            new Id.MethName(Optional.of(Mdf.imm), ".fromList", 1),
            List.of(call.recv()),
            call.t(),
            Mdf.imm,
            newVariants
          ), true));
        }
      }

      if (m.equals(new Id.MethName(".flow", 0)) || isMagic(Magic.SafeFlowSource, call.recv())) {
        if (parallelConstr.isPresent()) {
          var flowMethName = StringIds.$self.getMName(call.mdf(), call.name());
          var argList = args.stream()
            .map(arg -> arg.accept(gen, true))
            .collect(Collectors.joining(", "));
          return Optional.of(
            "rt.flows.FlowCreator.fromFlow(" +
              gen.visitCreateObj(new MIR.CreateObj(Mdf.imm, parallelConstr.get()), true) +
              ", " +
              call.recv().accept(gen, true) +
              "." + flowMethName +
              "(" + argList + ")" +
              ")"
          );
        }
      }

      System.err.println("Warning: No JS magic handler found for: " + e +
        "\nFalling back to Fearless JS implementation.");
      return Optional.empty();
    };
  }

}