package rt;

import userCode.FProgram;

public final class IO implements FProgram.base$46caps.IO_0 {
	public static final IO $self = new IO();
	@Override public FProgram.base.Void_0 printlnErr$mut$(String msg$) {
		System.err.println(msg$);
		return FProgram.base.Void_0.$self;
	}
	@Override public FProgram.base.Void_0 println$mut$(String msg$) {
		System.out.println(msg$);
		return FProgram.base.Void_0.$self;
	}
	@Override public FProgram.base.Void_0 print$mut$(String msg$) {
		System.out.print(msg$);
		return FProgram.base.Void_0.$self;
	}
	@Override public FProgram.base.Void_0 printErr$mut$(String msg$) {
		System.err.print(msg$);
		return FProgram.base.Void_0.$self;
	}
}
