interface FearlessBlock {
	interface Do { void of(); }
	interface ReturnStmt<R> { R of(); }
	interface Condition { boolean of(); }
	interface Continuation<X,R> { R of(X x, Block<R> self); }
	interface LoopBody<R> { ControlFlow<R> of(); }
	interface Info {/*..*/}
	interface Error {
		Error base = new Error(){};
		default <R> R of(Info a) {
			throw new java.lang.Error(new Throwable(){
				final Info info = a;
			});
		}
	}
	interface ControlFlow<R> {
		ControlFlow<Void> base = new ControlFlow<>(){};
		default <R> ControlFlow<R> $continue() { return (ControlFlow<R>) base; } // Intentionally use a bad type here
		default <R> ControlFlow<R> $break() {
			return new ControlFlow<>(){
				public Block<R> match(ControlFlowMatch<R> m) { return m.$break(); }
			};
		}
		default <R> ControlFlow<R> $return(R rv) {
			return new ControlFlow<>() {
				public Block<R> match(ControlFlowMatch<R> m) {
					return m.$return(rv);
				}
			};
		}
		default Block<R> match(ControlFlowMatch<R> m) { return m.$continue(); }
	}
	interface ControlFlowMatch<R> {
		Block<R> $continue();
		Block<R> $break();
		Block<R> $return(R rv);
	}

	interface Block<R> {
		Block<Void> base = new Block<>(){};
		default R $return(ReturnStmt<R> a) { return a.of(); }
		default BlockIf<R> $if(Condition a) {
			return a.of() ? (BlockIfTrue<R>)() -> this : (BlockIfFalse<R>)() -> this;
		}
		default <X> R var(ReturnStmt<X> x, Continuation<X,R> cont) {
			return cont.of(x.of(), this);
		}
		default Block<R> $do(Do a) { a.of(); return this._do(); } // some optimisation for Void values here?
		default Block<R> _do() { return this; }
		default Block<R> loop(LoopBody<R> a) {
			var $this = this;
			a.of().match(new ControlFlowMatch<>() {
				public Block<R> $continue() {
					return $this.loop(a);
				}
				public Block<R> $break() {
					return $this;
				}
				public Block<R> $return(R rv) {
					return (DecidedBlock<R>)() -> rv;
				}
			});
			return $this;
		}
	}
	interface DecidedBlock<R> extends Block<R> {
		R res();
		default R $return(ReturnStmt<R> a) { return this.res(); }
		default <X> R var(ReturnStmt<X> x, Continuation<X,R> cont) { return this.res(); }
		default BlockIf<R> $if(Condition a) { return (BlockIfFalse<R>)() -> this; }
		default Block<R> $do(Do a) { return this; } // some optimisation for Void values here?
		default Block<R> loop(LoopBody<R> a) { return this; }
	}
	interface BlockIf<R> {
		Block<R> outer();
		Block<R> $return(ReturnStmt<R> a);
		Block<R> error(ReturnStmt<Info> a);
		Block<R> $do(Do a);
	}
	interface BlockIfTrue<R> extends BlockIf<R> {
		default Block<R> $return(ReturnStmt<R> a) { return this._return(a.of()); }
		default DecidedBlock<R> _return(R x) { return () -> x; }
		default Block<R> error(ReturnStmt<Info> a) { return Error.base.of(a.of()); }
		default Block<R> $do(Do a) { a.of(); return this._do(); }
		default Block<R> _do() { return this.outer(); }
	}
	interface BlockIfFalse<R> extends BlockIf<R> {
		default Block<R> $return(ReturnStmt<R> a) { return this.outer(); }
		default Block<R> error(ReturnStmt<Info> a) { return this.outer(); }
		default Block<R> $do(Do a) { return this.outer(); }
	}

	interface User {
		User base = new User(){};
		default Integer num() {
			return new Block<Integer>(){}.var(() -> 10, (ten, b) -> b
					.$if(() -> 6 < ten)    .$do(() -> System.out.println("Hello"))
					.$return(() -> ten)
			);
		}
		default Integer earlyReturn() {
			return new Block<Integer>(){}.var(() -> 10, (ten, b) -> b
					.$if(() -> 6 < ten)    .$return(() -> 7)
					.$return(() -> ten)
			);
		}
		default Integer loopTen() {
			return new Block<Integer>(){}
					.var(() -> new Integer[]{0}, (acc, self) -> self
							.loop(() -> new Block<ControlFlow<Integer>>(){}
									.$if(() -> acc[0] == 10)  .$return(ControlFlow.base::$break)
									.$do(() -> acc[0] = acc[0] + 1)
									.$return(ControlFlow.base::$continue)
							)
							.$return(() -> acc[0])
					);
		}
	}

	static void main(String[] args) {
		System.out.println(User.base.num());
		System.out.println(User.base.earlyReturn());
		System.out.println(User.base.loopTen());
	}
}
