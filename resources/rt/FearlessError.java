package rt;

import userCode.FProgram;

import java.nio.charset.StandardCharsets;

class FearlessError extends RuntimeException {
	public FProgram.base.Info_0 info;
	public FearlessError(FProgram.base.Info_0 info) {
		super();
		this.info = info;
	}
	public String getMessage() { return new String(this.info.str$imm$().utf8(), StandardCharsets.UTF_8); }
}
