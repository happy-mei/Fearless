package rt;

import java.nio.charset.StandardCharsets;

class FearlessError extends RuntimeException {
	public base.Info_0 info;
	public FearlessError(base.Info_0 info) {
		super();
		this.info = info;
	}
	@Override public String getMessage() { return new String(this.info.msg$imm().utf8(), StandardCharsets.UTF_8); }
	@Override public String toString() { return this.getMessage(); }
}
