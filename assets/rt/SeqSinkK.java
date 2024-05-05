package rt;

public final class SeqSinkK implements base.flows._Sink_0 {
  public static final SeqSinkK $self = new SeqSinkK();
  @Override public base.flows._Sink_1 $hash$imm(base.flows._Sink_1 s_m$) {
    boolean[] hasThrown = {false};
    return new base.flows._Sink_1() {
      @Override
      public base.Void_0 stop$mut() {
        return s_m$.stop$mut();
      }

      @Override
      public base.Void_0 $hash$mut(Object x_m$) {
        if (hasThrown[0]) {
          return base.Void_0.$self;
        }
        try {
          return s_m$.$hash$mut(x_m$);
        } catch (Throwable t) {
          hasThrown[0] = true;
          return base.Void_0.$self;
        }
      }
    };
  }
}
