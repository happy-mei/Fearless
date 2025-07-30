package codegen.js;

import codegen.FlowSelector;
import codegen.MIR;
import id.Id;
import id.Mdf;
import magic.Magic;
import magic.MagicCallable;
import magic.MagicTrait;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record JsMagicImpls(MIRVisitor<String> gen, ast.Program p) implements magic.MagicImpls<String> {

  @Override public MagicTrait<MIR.E, String> int_(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        // Try to produce a JS numeric literal from a fearless literal, if present.
        var name = e.t().name().orElse(null);
        var lit = magic.Magic.getLiteral(p, name);
        if (lit.isPresent()) {
          String s = lit.get();
          // fearless ints can be like "+5" etc — normalize if needed
          if (s.startsWith("+")) s = s.substring(1);
          s = s.replace("_", ""); // remove any _ visual separators
          return Optional.of(s); // already a JS number
        }
        // Not a literal we can convert here — let normal codegen handle it.
        return Optional.empty();
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        String self = e.accept(gen, true);  // LHS
        String arg = args.isEmpty() ? "" : args.get(0).accept(gen, true);  // RHS

        // Handle string conversion
        if (m.equals(new Id.MethName(".str", 0))) {
          return Optional.of("String(" + self + ")");
        }
        // Arithmetic operators
        // Handle arithmetic operations
        if (m.equals(new Id.MethName("+", 1))) {
          return Optional.of("(" + self + " + " + arg + ")");
        }
        if (m.equals(new Id.MethName("*", 1))) {
          return Optional.of("(" + self + " * " + arg + ")");
        }
        return Optional.empty(); // Fallback for unhandled methods
      }
    };
  }

  // sign-extension "myNumber >>> 0" to get a 32-bit unsigned integer from a JS double
  @Override public MagicTrait<MIR.E, String> nat(MIR.E e) {
    return int_(e); // reuse int_ logic for now
  }

  @Override public MagicTrait<MIR.E, String> float_(MIR.E e) {
    return int_(e); // reuse int_ logic for now
  }

  @Override public MagicTrait<MIR.E, String> byte_(MIR.E e) {
    return int_(e); // reuse int_ logic for now
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
      public Optional<String> instantiate() {
        // This magic trait is not instantiated directly, so we return empty.
        return Optional.empty();
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args,
                                   EnumSet<MIR.MCall.CallVariant> variants,
                                   MIR.MT expectedT) {
//        boolean isTestMode = ((JsCodegen)gen).testMode;  // Access the flag

        if (m.equals(new Id.MethName("._fail", 0))) {
//          System.out.println("Assertion failed" + m);
          return Optional.of("""
            (function() {
             const err = new Error("Assertion failed");
             console.error(err);
             throw err;
            })()
            """);
        }

        if (m.equals(new Id.MethName("._fail", 1))) {
//          System.out.println("Assertion failed" + m);
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