package rt;

public interface Error {
	static <T> T throwFearlessError(base.Info_0 info) {
		throw new FearlessError(info);
	}
}
