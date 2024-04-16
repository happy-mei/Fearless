package rt;

import userCode.FProgram;

public interface Error {
	static <T> T throwFearlessError(FProgram.base.Info_0 info) {
		throw new FearlessError(info);
	}
}
