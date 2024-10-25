package program;

public final class TypeSystemFeatures {
  private boolean recMdf = false;
  public boolean recMdf() { return this.recMdf; }
  public TypeSystemFeatures recMdf(boolean enabled) { this.recMdf = enabled; return this; }

  private boolean hygienics = true;
  public boolean hygienics() { return this.hygienics; }
  public TypeSystemFeatures hygienics(boolean enabled) { this.hygienics = enabled; return this; }

  private boolean literalPromotions = true;
  public boolean literalPromotions() { return this.literalPromotions; }
  public TypeSystemFeatures literalPromotions(boolean enabled) { this.literalPromotions = enabled; return this; }
}
