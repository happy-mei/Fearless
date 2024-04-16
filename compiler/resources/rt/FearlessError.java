package base;

class FearlessError extends RuntimeException {
	public base.Info_0 info;
	public FearlessError(base.Info_0 info) {
		super();
		this.info = info;
	}
	public String getMessage() { return this.info.str$imm(); }
}
