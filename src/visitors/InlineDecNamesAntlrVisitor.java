package visitors;

import astFull.T;
import generated.FearlessBaseVisitor;
import generated.FearlessParser;
import id.Id;
import utils.Bug;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InlineDecNamesAntlrVisitor extends FearlessBaseVisitor<Void> {
  private final FullEAntlrVisitor parser;
  public final List<T.Alias> inlineDecs = new ArrayList<>();
  private String pkg;
  public InlineDecNamesAntlrVisitor(FullEAntlrVisitor parser, String pkg) {
    this.parser = parser;
    this.pkg = pkg;
  }

  @Override public Void visitLambda(FearlessParser.LambdaContext ctx) {
    Optional.ofNullable(ctx.topDec())
      .map(decCtx->{
        String cName = parser.visitFullCN(decCtx.fullCN());
        if (cName.contains(".")) {
          throw Bug.of("You may not declare a trait in a different package than the package the declaration is in.");
        }
        var longName = pkg + "." +cName;

//        var inG = Optional.ofNullable(decCtx.mGen())
//          .map(mGenCtx->parser.visitMGen(mGenCtx, true))
//          .flatMap(x->x)
//          .orElse(List.of());
        var inT=new Id.IT<T>(longName, List.of());
        return new T.Alias(inT, cName, Optional.ofNullable(parser.pos(decCtx)));
      })
      .ifPresent(inlineDecs::add);

    return super.visitLambda(ctx);
  }

  @Override public Void visitNudeProgram(FearlessParser.NudeProgramContext ctx) {
    String name = ctx.Pack().getText();
    assert name.startsWith("package ");
    assert name.endsWith("\n");
    this.pkg = name.substring("package ".length(),name.length()-1);
    return super.visitNudeProgram(ctx);
  }
}
