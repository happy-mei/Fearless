package program;

public record TypeSystemFeatures(boolean recMdf, boolean adapterSubtyping, boolean hygienics) {
  //TODO: note: I disabled the adapt subtyping it was going in stack overflow
  public TypeSystemFeatures() { this(true, false, true); }
  public static class TypeSystemFeaturesBuilder {
    boolean recMdf = true;
    boolean adapterSubTyping = true;
    boolean hygienics = true;
    public TypeSystemFeaturesBuilder allowRecMdf(boolean enabled) { this.recMdf = enabled; return this; }
    public TypeSystemFeaturesBuilder allowAdapterSubtyping(boolean enabled) { this.adapterSubTyping = enabled; return this; }
    public TypeSystemFeaturesBuilder allowHygienics(boolean enabled) { this.hygienics = enabled; return this; }
    public TypeSystemFeatures build() { return new TypeSystemFeatures(recMdf, adapterSubTyping, hygienics); }
  }
}
