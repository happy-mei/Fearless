package visitors;

import id.Id;
import id.Mdf;

public interface TypeVisitor<TT extends Id.Ty, R> {
  R visitInfer();
  R visitLiteral(Mdf mdf, Id.IT<TT> it);
  R visitRCX(Mdf mdf, Id.GX<TT> gx);
  R visitX(Id.GX<TT> x);
  R visitReadImm(Id.GX<TT> x);
}
