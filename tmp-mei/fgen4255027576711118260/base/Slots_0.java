package base;
public interface Slots_0{
Slots_0 $self = new Slots_0Impl();
base.Slot_1 $hash$imm();
static base.Slot_1 $hash$imm$fun(base.Slots_0 $this) {
  return ((base.Slot_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Slot_1) null;
}})
);
}
}