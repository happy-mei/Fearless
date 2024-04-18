package main;

public interface CheckMain {
  static void of(LogicMain main) {
    var parsed = main.parse();
    main.wellFormednessFull(parsed);
    var inferred = main.inference(parsed);
    main.wellFormednessCore(inferred);
    main.typeSystem(inferred);
    System.out.println("All checks passed");
  }
}
