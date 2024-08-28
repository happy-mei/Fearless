package rt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestNativeRegex {
	@Test void shouldCreateAndReleasePattern() {
		var patternStr = "hello".getBytes(StandardCharsets.UTF_8);
		new NativeRuntime.Regex(patternStr);
	}
	@Test void regexMatch() {
		var patternStr = "hello".getBytes(StandardCharsets.UTF_8);
		var regex = new NativeRuntime.Regex(patternStr);
		Assertions.assertTrue(regex.doesRegexMatch("hello".getBytes(StandardCharsets.UTF_8)));
		Assertions.assertTrue(regex.doesRegexMatch("hello!".getBytes(StandardCharsets.UTF_8)));
		Assertions.assertFalse(regex.doesRegexMatch("he".getBytes(StandardCharsets.UTF_8)));
	}
}
