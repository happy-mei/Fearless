import java.util.function.Supplier;

public class Bloop {
	interface Block<R> {
		default R _return(Supplier<R> p) { return p.get(); }
	}
	interface A {
		default B of() {
			return (B) new Block<>(){}._return(() -> new B(){});
		}
	}
	interface B {}

	public static void main(String[] args) {
		new A(){}.of();
	}
}
