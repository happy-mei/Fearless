package rt;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestNativeStrings {
	@Test void shouldThrowOnInvalidUtf8() {
		// Arrange
		var invalidUtf8 = new byte[]{-28};
		// Act/Assert
		assertThrows(NativeRuntime.StringEncodingError.class, ()->NativeRuntime.validateStringOrThrow(invalidUtf8));
	}
	@Test void shouldAllowEmptyUtf8() {
		// Arrange
		var invalidUtf8 = new byte[0];
		// Act/Assert
		NativeRuntime.validateStringOrThrow(invalidUtf8);
	}
	@Test void shouldProduceValidUtf8FromInvalidJava() {
		// Arrange
		var invalidUtf8 = new byte[]{-28};
		var jStr = new String(invalidUtf8, StandardCharsets.UTF_8);
		var utf8 = jStr.getBytes(StandardCharsets.UTF_8);
		// Act/Assert
		NativeRuntime.validateStringOrThrow(utf8);
		assertNotEquals(invalidUtf8, utf8);
	}
	@Test void shouldProduceA1GraphemeInvalidChar() {
		// Arrange
		var invalidUtf8 = new byte[]{-28};
		var jStr = new String(invalidUtf8, StandardCharsets.UTF_8);
		var utf8 = jStr.getBytes(StandardCharsets.UTF_8);
		// Act
		var size = NativeRuntime.indexString(utf8).length;
		// Assert
		NativeRuntime.validateStringOrThrow(utf8);
		assertEquals(1, size);
	}
	@Test void shouldHashConsistently() {
		var strA = "ABC".getBytes(StandardCharsets.UTF_8);
		assertEquals(NativeRuntime.hashString(strA), NativeRuntime.hashString(strA));
	}
	@Test void shouldHashDifferently() {
		var strA = "ABC".getBytes(StandardCharsets.UTF_8);
		var strB = "DEF".getBytes(StandardCharsets.UTF_8);
		assertNotEquals(NativeRuntime.hashString(strA), NativeRuntime.hashString(strB));
	}

	@Test void shouldNormaliseEqForSimpleStrings() {
		var strA = "ABC".getBytes(StandardCharsets.UTF_8);
		assertArrayEquals(strA, NativeRuntime.normaliseString(strA));
	}
	@Test void shouldNormaliseDifferentlyForComplexStrings() {
		var strA = "Å".getBytes(StandardCharsets.UTF_8); // just Å
		var strB = "Å".getBytes(StandardCharsets.UTF_8); // A + ring

		assert streamBytes(strA).toList() != streamBytes(strB).toList();
		assertEquals(
			streamBytes(NativeRuntime.normaliseString(strA)).toList(),
			streamBytes(NativeRuntime.normaliseString(strB)).toList()
		);
	}
	@Test void shouldNotChangeSizeWhenNormalising() {
		var str = "Å".getBytes(StandardCharsets.UTF_8); // A + ring

		assertEquals(
			NativeRuntime.indexString(str).length,
			NativeRuntime.indexString(NativeRuntime.normaliseString(str)).length
		);
	}

	private Stream<Byte> streamBytes(byte[] input) {
		var stream = Stream.<Byte>builder();
		for (var b : input) {
			stream.accept(b);
		}
		return stream.build();
	}
}
