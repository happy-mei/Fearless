package rt;

public final class IO implements base.caps.IO_0 {
	public static final IO $self = new IO();
	@Override public base.Void_0 printlnErr$mut(Str msg$) {
		NativeRuntime.printlnErr(msg$.utf8());
		return base.Void_0.$self;
	}
	@Override public base.Void_0 println$mut(Str msg$) {
		NativeRuntime.println(msg$.utf8());
		return base.Void_0.$self;
	}
	@Override public base.Void_0 print$mut(Str msg$) {
		NativeRuntime.print(msg$.utf8());
		return base.Void_0.$self;
	}
	@Override public base.Void_0 printErr$mut(Str msg$) {
		NativeRuntime.printErr(msg$.utf8());
		return base.Void_0.$self;
	}
}
