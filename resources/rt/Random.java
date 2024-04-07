package rt;

import userCode.FProgram;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

public interface Random {
	final class SeedGenerator implements FProgram.base$46caps.RandomSeed_0 {
		public static final SeedGenerator $self = new SeedGenerator();
		private final static SecureRandom rng = new SecureRandom();
		@Override public Long $35$mut$() {
			long res;
			do { res = ByteBuffer.wrap(rng.generateSeed(8)).getLong(); } while (res == 0);
			return res;
		}
	}
}
