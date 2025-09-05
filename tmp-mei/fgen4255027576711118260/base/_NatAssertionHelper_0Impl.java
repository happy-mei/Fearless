package base;
public record _NatAssertionHelper_0Impl() implements base._NatAssertionHelper_0 {
  public base.Void_0 assertEq$imm(Object expected_m$, Object actual_m$) {
  return this.assertEq$imm$Delegate((long) expected_m$, (long) actual_m$);
}

public base.Void_0 assertEq$imm$Delegate(long expected_m$, long actual_m$) {
  return  base._NatAssertionHelper_0.assertEq$imm$fun(expected_m$, actual_m$, this);
}


public base.Void_0 assertEq$imm(Object expected_m$, Object actual_m$, rt.Str message_m$) {
  return this.assertEq$imm$Delegate((long) expected_m$, (long) actual_m$, (rt.Str) message_m$);
}

public base.Void_0 assertEq$imm$Delegate(long expected_m$, long actual_m$, rt.Str message_m$) {
  return  base._NatAssertionHelper_0.assertEq$imm$fun(expected_m$, actual_m$, message_m$, this);
}


  
}
