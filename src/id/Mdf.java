package id;

import utils.Bug;

import java.util.Arrays;
import java.util.Optional;

public enum Mdf{
  mut,lent,read,iso,recMdf,mdf,imm;
  public boolean is(Mdf... valid){ return Arrays.stream(valid).anyMatch(v->this==v); }
  public boolean isMut(){return this==mut;}
  public boolean isLent(){return this==lent;}
  public boolean isRead(){return this==read;}
  public boolean isIso(){return this==iso;}
  public boolean isRecMdf(){return this==recMdf;}
  public boolean isMdf(){return this==mdf;}
  public boolean isImm(){return this==imm;}
  public boolean isHyg(){return isRead() || isLent();}
  public boolean couldBeHyg(){
    return isHyg() || isMdf() || isRecMdf();
  }
  public boolean isLikeMut(){return isRead() || isLent() || isMut();}
  public Mdf adapt(astFull.T t) {
    return this.adapt(t.mdf());
  }
  public Mdf adapt(ast.T t) {
    return this.adapt(t.mdf());
  }
  /**
   * How we see the type 'other' from the receiver 'this'
   * basically 'this.other'
   */
  public Mdf adapt(Mdf other) {
    if (this == other) { return this; }
    if (this == imm) { return imm; }
    if (this == iso) { return mut.adapt(other); }
    if (this == mut) { return other; }
    if (this == lent) {
      if (other == imm) { return other; }
      if (other == read) { return other; }
      if (other == recMdf){ return read; }
      if (other == mdf) { return read; }
      if (other == mut) { return lent; }
    }
    if (this == read) {
      if (other == imm) { return imm; }
      return recMdf;
    }
    System.err.println("uh oh adapt is undefined for "+this+" and "+other);
    throw Bug.unreachable();
  }

  public Optional<Mdf> restrict(Mdf mMdf) {
    if (mMdf.isImm() || (this.isImm() && mMdf.isRead())) { return Optional.of(imm); }
    if (isLikeMut() && mMdf.isRead() || (isRecMdf() && mMdf.isRead())) { return Optional.of(read); }
    if (isLent() && mMdf.isMut()){ return Optional.of(lent); }
    if (isLent() && mMdf.isIso()){ return Optional.of(mut); }
    if (mMdf.isLent()){ return Optional.of(lent); }
    if ((isMut() && mMdf.isIso()) || (isMut() && mMdf.isMut())) { return Optional.of(mut); }
    if (isRecMdf() && mMdf.is(lent, mut, iso)) { return Optional.of(lent); }
    System.err.println("uh oh restrict is undefined for "+this+" and "+mMdf);
    return Optional.empty();
  }
}