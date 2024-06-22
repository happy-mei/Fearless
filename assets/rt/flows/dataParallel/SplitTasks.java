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
    return Collections.unmodifiableList(split(List.of(task), n));
  }

  private static List<FlowOp_1> split(List<FlowOp_1> res, int n) {
    assert n >= 0;
    if (n == 0) {
      return res;
    }
    var didSplit = false;
    var splitTasks = new ArrayList<FlowOp_1>(res.size());
    for (var task : res) {
      var split = (FlowOp_1) task.split$mut().match$mut(new OptMatch_2() {
        @Override public Object some$mut(Object x_m$) {
          return x_m$;
        }
        @Override public Object empty$mut() {
          return null;
        }
      });
      if (split == null) {
        continue;
      }
      didSplit = true;
      splitTasks.add(split);
    }
    // Exit early if there's nothing left to split
    if (!didSplit) {
      return res;
    }

    var merged = new ArrayList<FlowOp_1>(res.size() + splitTasks.size());
    assert res.size() >= splitTasks.size();
    for (var i = 0; i < res.size(); ++i) {
      var lhs = res.get(i);
      merged.add(lhs);
      if (i >= splitTasks.size()) { continue; }
      var rhs = splitTasks.get(i);
      merged.add(rhs);
    }

    return split(merged, n - 1);
  }
}
