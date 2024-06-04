package rt.flows.dataParallel;

public interface ParallelStrategies {
  void seqOnly();
  void oneParOneSeq();
  void manyPar();

  default void run(int nTasks) {
    System.out.println("hmm "+nTasks);
    if (nTasks == 2) {
      oneParOneSeq();
      return;
    }
    if (nTasks <= 3) {
      seqOnly();
      return;
    }
    manyPar();
  }
}
