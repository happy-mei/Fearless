package codegen.optimisations;

import codegen.MIR;
import codegen.MIRCloneVisitor;
import id.Id;
import magic.Magic;
import magic.MagicImpls;
import utils.Bug;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockOptimisation implements MIRCloneVisitor {
  private final MagicImpls<?> magic;
  private Map<MIR.FName, MIR.Fun> funs;
  public BlockOptimisation(MagicImpls<?> magic) {
    this.magic = magic;
  }

  @Override public MIR.Package visitPackage(MIR.Package pkg) {
    this.funs = pkg.funs().stream().collect(Collectors.toMap(MIR.Fun::name, Function.identity()));
    return MIRCloneVisitor.super.visitPackage(pkg);
  }

  @Override public MIR.Fun visitFun(MIR.Fun fun) {
    if (!(fun.body() instanceof MIR.MCall call)) { return MIRCloneVisitor.super.visitFun(fun); }
    var isBlock = this.magic.isMagic(Magic.Block, call.recv());
    if (!isBlock) {
      return MIRCloneVisitor.super.visitFun(fun);
    }
    return this.visitBlockCall(call).map(fun::withBody).orElse(MIRCloneVisitor.super.visitFun(fun));
  }

  private Optional<MIR.E> visitBlockCall(MIR.MCall call) {
    var stmts = new ArrayDeque<MIR.Block.BlockStmt>();
    var res = flattenBlock(call, stmts);
    if (res == FlattenStatus.INVALID) { return Optional.empty(); }
    if (!(stmts.getLast() instanceof MIR.Block.BlockStmt.Return)) { return Optional.empty(); }
    assert res == FlattenStatus.FLATTENED;
    return Optional.of(new MIR.Block(call, Collections.unmodifiableCollection(stmts), call.t()));
  }

  private enum FlattenStatus { CONTINUE, INVALID, FLATTENED }
  private FlattenStatus flattenBlock(MIR.E expr, Deque<MIR.Block.BlockStmt> stmts) {
    return switch (expr) {
      case MIR.MCall mCall -> {
        if (mCall.name().equals(new Id.MethName("#", 0)) && this.magic.isMagic(Magic.BlockK, mCall.recv())) {
          yield FlattenStatus.FLATTENED;
        }
        if (mCall.name().equals(new Id.MethName(".return", 1))) {
          var res = this.visitReturn(mCall.args().getFirst());
          if (res.isEmpty()) { yield  FlattenStatus.INVALID; }
          stmts.offerFirst(new MIR.Block.BlockStmt.Return(res.get()));
        } else if (mCall.name().equals(new Id.MethName(".do", 1))) {
          var res = this.visitReturn(mCall.args().getFirst());
          if (res.isEmpty()) {
            yield FlattenStatus.INVALID;
          }
          stmts.offerFirst(new MIR.Block.BlockStmt.Do(res.get()));
        } else if (mCall.name().equals(new Id.MethName(".loop", 1))) {
          // maybe don't inline this, so we can optimise the loop body too
          var res = this.visitReturn(mCall.args().getFirst());
          if (res.isEmpty()) {
            yield FlattenStatus.INVALID;
          }
          stmts.offerFirst(new MIR.Block.BlockStmt.Loop(res.get()));
        } else if (mCall.name().equals(new Id.MethName(".if", 1))) {
          var res = this.visitReturn(mCall.args().getFirst());
          if (res.isEmpty()) {
            yield FlattenStatus.INVALID;
          }
          stmts.offerFirst(new MIR.Block.BlockStmt.If(res.get()));
        } else {
          // TODO: .var
          // TODO: .error
          // TODO: .varIso
          yield FlattenStatus.INVALID;
        }
        yield flattenBlock(mCall.recv(), stmts);
      }
      case MIR.BoolExpr boolExpr -> throw Bug.todo();
      case MIR.CreateObj ignored -> FlattenStatus.INVALID;
      case MIR.X ignored -> FlattenStatus.INVALID;
      case MIR.Block ignored -> throw Bug.unreachable();
    };
  }

  private Optional<MIR.E> visitReturn(MIR.E fn) {
    if (!(fn instanceof MIR.CreateObj k)) { return Optional.empty(); }
    if (!this.magic.isMagic(Magic.ReturnStmt, k)) { return Optional.empty(); }
    if (k.meths().size() != 1) { return Optional.empty(); }
    var m = k.meths().getFirst();
    assert m.sig().name().equals(new Id.MethName("#", 0));
    var body = this.funs.get(m.fName()).body();
    return Optional.of(body);
  }
}
