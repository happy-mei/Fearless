package rt.flows.dataParallel;

import base.OptMatch_2;
import base.flows.FlowOp_1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SplitTasks {
  /**
   * Split a task up to n times. If the task is no longer splittable, there will be less than n splits.
   * Ordering is kept consistent.
   *
   * @param n The number of times to split the tasks
   * @return A collection of split tasks
   */
  public static List<FlowOp_1> of(FlowOp_1 task, int n) {
    assert n >= 1;
    if (n == 1) {
      return List.of(task);
    }
    return Collections.unmodifiableList(split(List.of(task), n, n));
  }

  /**
   * A FlowOp that has been split up
   * @param lhs The original (potentially mutated) flow op.
   * @param rhs The split flow op. It will be null if the original flow op could not be split.
   */
  record SplitTask(FlowOp_1 lhs, FlowOp_1 rhs) {}
  private static List<FlowOp_1> split(List<FlowOp_1> res, int n, int max) {
    assert n >= 0;
    if (n == 0 || res.size() >= max) {
      return res;
    }
    var didSplit = false;
    var splitTasks = new ArrayList<SplitTask>(res.size());
    for (var task : res) {
      var split = (FlowOp_1) task.split$mut().match$mut(new OptMatch_2() {
        @Override public Object some$mut(Object x_m$) {
          return x_m$;
        }
        @Override public Object empty$mut() {
          return null;
        }
      });
      splitTasks.add(new SplitTask(task, split));
      if (split == null) { continue; }
      didSplit = true;
    }
    // Exit early if there's nothing left to split
    if (!didSplit) {
      return res;
    }

    assert res.size() == splitTasks.size();
    var merged = new ArrayList<FlowOp_1>(splitTasks.size() * 2);
    for (var task : splitTasks) {
      merged.add(task.lhs);
      if (task.rhs == null) { continue; }
      merged.add(task.rhs);
    }

    return split(merged, n - 1, max);
  }
}
