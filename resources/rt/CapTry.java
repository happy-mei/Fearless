package rt;

public final class CapTry implements base.caps.CapTry_0 {
	public static final CapTry $self = new CapTry();
	@Override public base.Res_2 $hash$mut(base.caps.TryBody_1 try$) {
		try { return base.Res_0.$self.ok$imm(try$.$hash$mut()); }
		catch(FearlessError _$err) { return base.Res_0.$self.err$imm(_$err.info); }
		catch(java.lang.StackOverflowError _$err) { return base.Res_0.$self.err$imm(base.FInfo_0.$self.str$imm("Stack overflowed")); }
		catch(Throwable _$err) { return base.Res_0.$self.err$imm(base.FInfo_0.$self.str$imm(_$err.getLocalizedMessage())); }
	}
}
