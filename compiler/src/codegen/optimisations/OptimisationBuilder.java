package codegen.optimisations;

import codegen.MIR;
import codegen.MIRCloneVisitor;
import magic.MagicImpls;

import java.util.ArrayList;
import java.util.List;

public class OptimisationBuilder {
  private final List<MIRCloneVisitor> passes = new ArrayList<>();
  private final MagicImpls<?> magic;
  public OptimisationBuilder(MagicImpls<?> magic) {
    this.magic = magic;
    // Add required passes (i.e. unsound without them)
    this.passes.add(new DataParallelInvalidator());
  }
  public MIR.Program run(MIR.Program p) {
    var p_ = p;
    for (var pass : this.passes) {
      p_ = pass.visitProgram(p_);
    }
    return p_;
  }
  public OptimisationBuilder withBoolIfOptimisation() {
    passes.add(new BoolIfOptimisation(magic));
    return this;
  }
  public OptimisationBuilder withDevirtualisationOptimisation() {
    passes.add(new DevirtualisationOptimisation(magic));
    return this;
  }
  public OptimisationBuilder withBlockOptimisation() {
    passes.add(new BlockOptimisation(magic));
    return this;
  }
}
