package rt;

import userCode.FProgram;

public final class CapTry implements FProgram.base$46caps.CapTry_0 {
	public static final CapTry _$self = new CapTry();
	@Override public FProgram.base.Res_2 $35$mut$(FProgram.base$46caps.TryBody_1 try$) {
		try { return FProgram.base.Res_0._$self.ok$imm$(try$.$35$mut$()); }
		catch(FearlessError _$err) { return FProgram.base.Res_0._$self.err$imm$(_$err.info); }
		catch(java.lang.StackOverflowError _$err) { return FProgram.base.Res_0._$self.err$imm$(FProgram.base.FInfo_0._$self.str$imm$("Stack overflowed")); }
		catch(Throwable _$err) { return FProgram.base.Res_0._$self.err$imm$(FProgram.base.FInfo_0._$self.str$imm$(_$err.getLocalizedMessage())); }
	}
}
