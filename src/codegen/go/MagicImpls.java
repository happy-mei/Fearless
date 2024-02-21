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

import static magic.MagicImpls.getLiteral;

public record MagicImpls(PackageCodegen gen, ast.Program p) implements magic.MagicImpls<String> {
  @Override public MagicTrait<MIR.E, String> int_(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public String instantiate() {
        var lit = getLiteral(p, name);
        try {
          return lit
            .map(lambdaName->"int64("+Long.parseLong(lambdaName.replace("_", ""), 10)+")")
            .orElseGet(()->"int64("+e.accept(gen, true)+")");
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Int");
        }
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> uint(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public String instantiate() {
        var lit = getLiteral(p, name);
        try {
          return lit
            .map(lambdaName->"uint64("+Long.parseUnsignedLong(lambdaName.substring(0, lambdaName.length()-1).replace("_", ""), 10)+")")
            .orElseGet(()->"uint64("+e.accept(gen, true)+")");
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "UInt");
        }
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> float_(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public String instantiate() {
        var lit = getLiteral(p, name);
        try {
          return lit
            .map(lambdaName->"float64("+Double.parseDouble(lambdaName.replace("_", ""))+")")
            .orElseGet(()->"float64("+e.accept(gen, true)+")");
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Float");
        }
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> str(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public String instantiate() {
        var lit = getLiteral(p, name);
        return lit.orElseGet(()->"((String)"+e.accept(gen, true)+")");
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
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

  @Override public MagicTrait<MIR.E, String> assert_(MIR.E e) {
    return null;
  }

  @Override public MagicCallable<MIR.E, String> variantCall(MIR.E e) {
    return null;
  }
}
