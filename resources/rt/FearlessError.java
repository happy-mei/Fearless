package rt;

import userCode.FProgram;

class FearlessError extends RuntimeException {
	public FProgram.base.Info_0 info;
	public FearlessError(FProgram.base.Info_0 info) {
		super();
		this.info = info;
	}
	public String getMessage() { return this.info.str$imm$(); }
}
