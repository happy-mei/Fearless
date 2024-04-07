package rt;

public final class IO implements base.caps.IO_0 {
	public static final IO $self = new IO();
	@Override public base.Void_0 printlnErr$mut(String msg$) {
		System.err.println(msg$);
		return base.Void_0.$self;
	}
	@Override public base.Void_0 println$mut(String msg$) {
		System.out.println(msg$);
		return base.Void_0.$self;
	}
	@Override public base.Void_0 print$mut(String msg$) {
		System.out.print(msg$);
		return base.Void_0.$self;
	}
	@Override public base.Void_0 printErr$mut(String msg$) {
		System.err.print(msg$);
		return base.Void_0.$self;
	}
}
