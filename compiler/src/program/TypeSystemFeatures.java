package program;

public record TypeSystemFeatures(boolean recMdf, boolean adapterSubtyping, boolean hygienics) {
  public static TypeSystemFeatures of() { return new TypeSystemFeaturesBuilder().build(); }
  public static class TypeSystemFeaturesBuilder {
    boolean recMdf = false;
    boolean adapterSubTyping = false;
    boolean hygienics = true;
    public TypeSystemFeaturesBuilder allowRecMdf(boolean enabled) { this.recMdf = enabled; return this; }
    public TypeSystemFeaturesBuilder allowAdapterSubtyping(boolean enabled) { this.adapterSubTyping = enabled; return this; }
    public TypeSystemFeaturesBuilder allowHygienics(boolean enabled) { this.hygienics = enabled; return this; }
    public TypeSystemFeatures build() { return new TypeSystemFeatures(recMdf, adapterSubTyping, hygienics); }
  }
}
