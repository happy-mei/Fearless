package rt;

import userCode.FProgram;

import java.nio.charset.StandardCharsets;

public interface Str extends FProgram.base.Str_0 {
	byte[] utf8();
	int[] graphemes();

	static Str fromJavaStr(String str) {
		var utf8 = str.getBytes(StandardCharsets.UTF_8);
		return fromTrustedUtf8(utf8);
	}
	static Str fromUtf8(byte[] utf8) {
		NativeRuntime.validateStringOrThrow(utf8);
		return fromTrustedUtf8(utf8);
	}
	static Str fromTrustedUtf8(byte[] utf8) {
		return new Str(){
			private int[] GRAPHEMES = null;
			@Override public byte[] utf8() { return utf8; }
			@Override public int[] graphemes() {
				if (GRAPHEMES == null) { GRAPHEMES = NativeRuntime.indexString(utf8); }
				return GRAPHEMES;
			}
		};
	}

	@Override default FProgram.base.Str_0 str$read$() {
		return this;
	}
	@Override default FProgram.base.Bool_0 $61$61$readOnly$(FProgram.base.Str_0 other$) {
		Str other = (Str) other$;
		return this.graphemes().equals(other.graphemes()) ? FProgram.base.True_0._$self : FProgram.base.False_0._$self;
	}
	@Override default FProgram.base.Str_0 $43$readOnly$(FProgram.base.Str_0 other$) {
		Str other = (Str) other$;
		var a = this.utf8();
		var b = other.utf8();
		var res = new byte[a.length + b.length];
		System.arraycopy(a, 0, res, 0, a.length);
		System.arraycopy(b, 0, res, a.length, b.length);
		return fromTrustedUtf8(res);
	}
	@Override default Long size$readOnly$() {
		return (long) this.graphemes().length;
	}
	@Override default FProgram.base.Void_0 assertEq$readOnly$(FProgram.base.Str_0 other$) {
//		return FProgram.base.$95StrHelpers_0._$self.assertEq$imm$(this, other$);
		throw new RuntimeException("tbd");
	}
	@Override default FProgram.base.Bool_0 isEmpty$readOnly$() {
		return this.utf8().length == 0 ? FProgram.base.True_0._$self : FProgram.base.False_0._$self;
	}
	@Override default FProgram.base.Str_0 toImm$readOnly$() {
		return this.str$read$();
	}
}
