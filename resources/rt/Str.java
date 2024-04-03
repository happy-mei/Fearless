package rt;

import userCode.FProgram;

public interface Str extends FProgram.base.Str_0 {
	String javaEncoding();
	byte[][] chars();

	static Str fromJavaStr(String str) {
		return new Str(){
			private byte[][] CHARS = null;
			@Override public String javaEncoding() { return str; }
			@Override public byte[][] chars() {
				if (CHARS == null) { CHARS = FearlessUnicode.parse(str); }
				return CHARS;
			}
		};
	}

	@Override default FProgram.base.Str_0 str$read$() {
		return this;
	}
	@Override default FProgram.base.Bool_0 $61$61$readOnly$(FProgram.base.Str_0 other$) {
		Str other = (Str) other$;
		return this.javaEncoding().equals(other.javaEncoding()) ? FProgram.base.True_0._$self : FProgram.base.False_0._$self;
	}
	@Override default FProgram.base.Str_0 $43$readOnly$(FProgram.base.Str_0 other$) {
		Str other = (Str) other$;
		return Str.fromJavaStr(this.javaEncoding() + other.javaEncoding());
	}
	@Override default Long size$readOnly$() {
		return (long) this.chars().length;
	}
	@Override default FProgram.base.Void_0 assertEq$readOnly$(FProgram.base.Str_0 other$) {
//		return FProgram.base.$95StrHelpers_0._$self.assertEq$imm$(this, other$);
		throw new RuntimeException("tbd");
	}
	@Override default FProgram.base.Bool_0 isEmpty$readOnly$() {
		return this.javaEncoding().isEmpty() ? FProgram.base.True_0._$self : FProgram.base.False_0._$self;
	}
	@Override default FProgram.base.Str_0 toImm$readOnly$() {
		return this.str$read$();
	}
}

final class ExampleStr implements Str {
	public static final FProgram.ExampleStr _$self = new FProgram.ExampleStr();
	private static final String JAVA_ENCODING = "Hello, World!";
	private static final byte[][] CHARS = new byte[][]{{72},{101},{108},{108},{111},{44},{32},{87},{111},{114},{108},{100},{33}};
	@Override public String javaEncoding() { return JAVA_ENCODING; }
	@Override public byte[][] chars() { return CHARS; }
}
