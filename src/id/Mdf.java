package id;

import utils.Bug;

public enum Mdf{
  mut,lent,read,iso,recMdf,mdf,imm;
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
    if (this == imm) { return imm; }
    if (this == mut) { return other; }
    if (this == lent) {
      if (other == imm) { return other; }
      if (other == read) { return recMdf; }
      if (other == mut) { return lent; }
    }
    if (this == read) {
      if (other == imm) { return imm; }
      return recMdf;
    }
    throw Bug.unreachable();
  }
}