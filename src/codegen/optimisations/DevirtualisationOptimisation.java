package codegen.optimisations;

import codegen.MIR;
import codegen.MIRCloneVisitor;
import id.Id;
import magic.MagicImpls;
import program.typesystem.XBs;
import utils.Bug;
import utils.OneOr;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This optimisation turns method calls into static function calls
 * if that method's function does not capture its self reference or anything out of scope at the call-site.
 * We apply this optimisation in waves until no more changes are made.
 * This applies to method calls on singleton-able lambdas and method calls on object literals. If there is more
 * indirection involved we will not apply the optimisation.
 *
 * This isn't quite ready for use yet.
 */
public class DevirtualisationOptimisation implements MIRCloneVisitor {
  private final MagicImpls<?> magic;
  private boolean hasActed = true;
  private Map<MIR.FName, MIR.Fun> funs;
  private MIR.Program p;
  private Set<String> gamma;
  public DevirtualisationOptimisation(MagicImpls<?> magic) {
    this.magic = magic;
  }

  @Override public MIR.Program visitProgram(MIR.Program p) {
    this.p = p;
    this.funs = p.pkgs().stream().flatMap(pkg->pkg.funs().stream()).collect(Collectors.toMap(MIR.Fun::name, Function.identity()));
    if (!hasActed) {
      return p;
    }
    hasActed = false;
    var res = MIRCloneVisitor.super.visitProgram(p);
    return this.visitProgram(res);
  }

  @Override public MIR.Package visitPackage(MIR.Package pkg) {
    return MIRCloneVisitor.super.visitPackage(pkg);
  }

  @Override public MIR.Meth visitMeth(MIR.Meth meth) {
    var gamma = new HashSet<>(meth.captures());
    meth.sig().xs().stream().map(MIR.X::name).forEach(gamma::add);
    this.gamma = Collections.unmodifiableSet(gamma);
    return MIRCloneVisitor.super.visitMeth(meth);
  }

  @Override public MIR.Fun visitFun(MIR.Fun fun) {
    this.gamma = fun.args().stream().map(MIR.X::name).collect(Collectors.toUnmodifiableSet());
    return MIRCloneVisitor.super.visitFun(fun);
  }

  @Override public MIR.E visitMCall(MIR.MCall call, boolean checkMagic) {
    if (call.variant().contains(MIR.MCall.CallVariant.Standard) || this.magic.get(call.recv()).isPresent()) {
      return MIRCloneVisitor.super.visitMCall(call, checkMagic);
    }

    assert call.name().mdf().isPresent();
    var subject_ = call.recv().t().name();
    if (subject_.isEmpty() || MagicImpls.getLiteral(this.magic.p(), subject_.get()).isPresent()) { return MIRCloneVisitor.super.visitMCall(call, checkMagic); }
    if (!(call.recv() instanceof MIR.CreateObj k)) { return MIRCloneVisitor.super.visitMCall(call, checkMagic); }
    var recvK_ = k.meths().isEmpty() ? this.p.of(subject_.get()).singletonInstance() : Optional.of(k);
    if (recvK_.isEmpty()) { return MIRCloneVisitor.super.visitMCall(call, checkMagic); }
    var recvK = recvK_.get();
    var meth = OneOr.of(
      "Invalid number of method candidates when applying 'DevirtualisationOptimisation'",
      recvK.meths().stream().filter(m->m.sig().name().equals(call.name()) && m.sig().mdf() == call.name().mdf().get())
    );
//    var expectedGamma = new HashSet<>(meth.captures());
//    meth.sig().xs().stream().map(MIR.X::name).forEach(expectedGamma::add);
    var fun = funs.get(meth.fName());
    if (meth.capturesSelf() || fun.args().stream().anyMatch(x->!this.gamma.contains(x.name()))) {
      return MIRCloneVisitor.super.visitMCall(call, checkMagic);
    }

    System.out.println("DevirtualisationOptimisation: " + call.name() + " on " + recvK.t().name().get() + " in " + fun.name());
    this.hasActed = true;
    return this.funs.get(meth.fName()).body().accept(this, true);
  }
}
