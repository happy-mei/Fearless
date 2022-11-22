package id;

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
}