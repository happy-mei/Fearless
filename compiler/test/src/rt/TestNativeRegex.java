package rt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import rt.NativeRuntime.*;

public class TestNativeRegex {
	@Test void shouldCreateAndReleasePattern() {
		var patternStr = "hello".getBytes(StandardCharsets.UTF_8);
		new Regex(patternStr);
	}
	@Test void regexMatch() {
		var patternStr = "hello".getBytes(StandardCharsets.UTF_8);
		var regex = new Regex(patternStr);
		Assertions.assertTrue(regex.doesRegexMatch("hello".getBytes(StandardCharsets.UTF_8)));
		Assertions.assertTrue(regex.doesRegexMatch("hello!".getBytes(StandardCharsets.UTF_8)));
		Assertions.assertFalse(regex.doesRegexMatch("he".getBytes(StandardCharsets.UTF_8)));
	}
	@Test void invalidRegex() {
		var patternStr = "[".getBytes(StandardCharsets.UTF_8);
		try {
			new Regex(patternStr);
			Assertions.fail("Did not throw");
		} catch (Regex.InvalidRegexError e) {
			Assertions.assertEquals(e.getMessage(), """
				regex parse error:
				    [
				    ^
				error: unclosed character class""");
		}
	}
}
