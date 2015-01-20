/*
 * Copyright 2012 Sébastien Aupetit <sebtic@projectforge.org>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.projectsforge.utils.tasksexecutor;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class TestPerformance {

  class PerformanceTask extends RecursiveTask {

    private final Random localRandom = new Random();

    private final long sumCount;

    private final List<PerformanceTask> subTasksList;

    private final PerformanceTask[] subTasksArray;

    PerformanceTask(final long sumCount, final List<PerformanceTask> subTasks) {
      this.sumCount = sumCount;
      this.subTasksList = subTasks;
      this.subTasksArray = subTasks.toArray(new PerformanceTask[0]);
    }

    public int countTasks() {
      int count = 1;
      for (final PerformanceTask task : subTasksList) {
        count += task.countTasks();
      }
      return count;
    }

    @Override
    public void run() throws RecursiveTaskExecutorException {
      @SuppressWarnings("unused")
      double sum = 0;
      for (long i = 0; i < sumCount; ++i) {
        sum += localRandom.nextInt(100);
      }
      if (asArray) {
        RecursiveTaskExecutorFactory.getInstance().execute(subTasksArray);
      } else {
        RecursiveTaskExecutorFactory.getInstance().execute(subTasksList);
      }

    }
  }

  private final boolean asArray = true;

  Random random = new Random(1);

  long maxTreeDepth = 7;

  int maxTryCount = 50;

  PerformanceTask getTaskTree(final int treeDepth) {

    final ArrayList<PerformanceTask> subTasks = new ArrayList<>();
    if (treeDepth <= maxTreeDepth) {
      final int childrenCount = 1 + random.nextInt(7);
      for (int i = 0; i < childrenCount; ++i) {
        subTasks.add(getTaskTree(treeDepth + 1));
      }
    }

    return new PerformanceTask(random.nextInt(10000), subTasks);
  }

  @Test
  public void testPerformance() throws RecursiveTaskExecutorException {
    final PerformanceTask taskTree = getTaskTree(1);
    System.err.println("RecursiveTask tree size : " + taskTree.countTasks());

    timeExecution(taskTree);
  }

  public void timeExecution(final PerformanceTask taskTree) throws RecursiveTaskExecutorException {
    // warm up
    System.err.println("Warming up 20 times");
    for (int i = 0; i < 20; ++i) {
      RecursiveTaskExecutorFactory.getInstance().execute(new PerformanceTask[] { taskTree });
    }

    System.err.println("Timing execution");
    final long start = System.currentTimeMillis();
    final long startThCpu = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
    final long startThUser = ManagementFactory.getThreadMXBean().getCurrentThreadUserTime();
    for (int i = 0; i < maxTryCount; ++i) {
      RecursiveTaskExecutorFactory.getInstance().execute(new PerformanceTask[] { taskTree });
    }
    final long stop = System.currentTimeMillis();
    final long stopThCpu = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
    final long stopThUser = ManagementFactory.getThreadMXBean().getCurrentThreadUserTime();

    System.err.println("Average execution time (ms) :" + ((stop - start) / maxTryCount));
    System.err.println("Average thread cpu execution time (ms) :"
        + ((stopThCpu - startThCpu) / maxTryCount / 1000000));
    System.err.println("Average thread user execution time (ms) :"
        + ((stopThUser - startThUser) / maxTryCount / 1000000));
  }

}
