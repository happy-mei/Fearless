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
  public boolean isLikeMut(){return isRead() || isLent() || isMut();}
  public Mdf adapt(astFull.T t) {
    return this.adapt(t.mdf());
  }
  public Mdf adapt(ast.T t) {
    return this.adapt(t.mdf());
  }
  public Mdf adapt(Mdf other) {
    if (this == other) { return this; }
    if (this == imm) { return imm; }
    if (this == mut) { return other; }
    if (this == lent) {
      if (other == imm) { return other; }
      if (other == read) { return other; }
      if (other == mut) { return lent; }
    }
    if (this == read) {
      if (other == imm) { return imm; }
      return recMdf;
    }
    throw Bug.unreachable();
  }

  public Optional<Mdf> restrict(Mdf mMdf) {
    if (mMdf.isImm() || (this.isImm() && mMdf.isRead())) { return Optional.of(imm); }
    if (this.isLikeMut() && mMdf.isRead()) { return Optional.of(read); }
    if (isLent() && mMdf.isMut()){ return Optional.of(lent); }
    if (isLent() && mMdf.isLent()){ return Optional.of(lent); }
    if (isMut() && mMdf.isLent()){ return Optional.of(lent); }
    if ((this.isMut() && mMdf.isIso()) || (this.isMut() && mMdf.isMut())) { return Optional.of(mut); }
    return Optional.empty();
  }
}