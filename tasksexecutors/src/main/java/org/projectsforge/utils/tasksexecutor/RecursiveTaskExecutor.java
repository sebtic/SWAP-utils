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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.RandomAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class allowing an efficient execution of independent tasks that can also
 * require the executions of other independent tasks. It's a parallel
 * decomposition into independent sub tasks.
 * 
 * @author Sébastien Aupetit
 */
public class RecursiveTaskExecutor {

  /**
   * Simple implementation of List based on an array for which the toArray()
   * method do not duplicate the array (for internal use only).
   */
  private static class DirectList<T> extends AbstractList<T> implements RandomAccess {

    private final T[] a;

    DirectList(final T[] array) {
      a = array;
    }

    @Override
    public T get(final int index) {
      return a[index];
    }

    @Override
    public T set(final int index, final T element) {
      final T oldValue = a[index];
      a[index] = element;
      return oldValue;
    }

    @Override
    public int size() {
      return a.length;
    }

    @Override
    public Object[] toArray() {
      return a;
    }

  }

  /** The Constant NOTITY_WAIT. */
  private static final int NOTITY_WAIT = 50;

  /** The thread group. */
  private final ThreadGroup threadGroup;

  /** The thread count. */
  private final int threadCount;

  /** The thread id. */
  private int threadId = 0;

  /** The running thread. */
  int runningThread = 0;

  /** The threads call stacks. */
  final ThreadsCallStacks threadsCallStacks = new ThreadsCallStacks();

  /** The task list. */
  private final ArrayList<RecursiveTask> taskList = new ArrayList<>(1024);

  /** The notification object. */
  private final Object notificationObject = new Object();

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(RecursiveTaskExecutor.class);

  private final RecursiveTask[] EMPTY_ARRAY = new RecursiveTask[0];

  /**
   * Instantiates a new task executor.
   * 
   * @param threadGroup the thread group
   * @param threadCount the thread count
   */
  public RecursiveTaskExecutor(final ThreadGroup threadGroup, final int threadCount) {
    this.threadGroup = threadGroup;
    this.threadCount = threadCount;
  }

  /**
   * All done.
   * 
   * @param <T> the generic type
   * @param tasks the tasks
   * @return true, if successful
   */
  private <T extends RecursiveTask> boolean allDone(final T[] tasks) {
    boolean done = true;
    for (int i = tasks.length - 1; i >= 0; --i) {
      if (!tasks[i].executed) {
        done = false;
        break;
      }
    }
    return done;
  }

  /**
   * Enque tasks.
   * 
   * @param tasks the tasks
   */
  private <T extends RecursiveTask> void enqueTasks(final T[] tasks) {
    final int depth = prepareTasks(tasks);

    synchronized (taskList) {
      // we insert in the tasklist following depth. We compute deepest first to
      // reduce stack use.
      int insertIndex = 0;

      final int len = taskList.size();
      for (int i = len - 1; i >= 0; --i) {
        if (taskList.get(i).depth <= depth) {
          insertIndex = i + 1;
          break;
        }
      }

      // Use DirectList to avoid duplication of the array when toArray is called
      // by addAll
      taskList.addAll(insertIndex, new DirectList<>(tasks));
    }

    // notify that new tasks were added
    synchronized (notificationObject) {
      notificationObject.notifyAll();
    }

  }

  /**
   * Ensure thread availability.
   */
  private void ensureThreadAvailability() {
    final int missing;
    int oldThreadId;
    synchronized (this) {
      missing = threadCount - runningThread;
      runningThread = threadCount;
      oldThreadId = threadId;
      threadId += missing;
    }
    // create the missing threads outside of the critical section
    for (int i = 0; i < missing; ++i) {
      new RecursiveExecutorThread(this, threadGroup, oldThreadId + i).start();
    }
  }

  /**
   * Execute the tasks of a collection. The collection is serialized to an array
   * before calling {link {@link #execute(RecursiveTask[])}.
   * 
   * @param <T> the generic type
   * @param tasks the tasks
   * @throws RecursiveTaskExecutorException the recursive task executor
   *           exception
   */
  public <T extends RecursiveTask> void execute(final Collection<T> tasks) throws RecursiveTaskExecutorException {
    if (tasks.isEmpty()) {
      return;
    }
    if (tasks.size() == 1 || threadCount == 0) {
      // direct sequential execution
      prepareTasks(tasks);
      for (final T task : tasks) {
        task.execute();
      }
    } else {
      execute(tasks.toArray(EMPTY_ARRAY));
    }
  }

  /**
   * Execute.
   * 
   * @param <T> the generic type
   * @param tasks the tasks
   * @throws RecursiveTaskExecutorException the recursive task executor
   *           exception
   */
  public <T extends RecursiveTask> void execute(final T[] tasks) throws RecursiveTaskExecutorException {
    if (tasks.length == 0) {
      return;
    }

    if (tasks.length == 1 || threadCount == 0) {
      prepareTasks(tasks);
      for (final T task : tasks) {
        task.execute();
      }
    } else {
      // parallel execution
      ensureThreadAvailability();
      parallelExecute(tasks);
    }

    for (int i = tasks.length - 1; i >= 0; i--) {
      if (tasks[i].getException() != null) {
        throw new RecursiveTaskExecutorException(tasks);
      }
    }
  }

  /**
   * Execute sequentially the tasks of a collection.
   * 
   * @param <T> the generic type
   * @param tasks the tasks
   * @throws RecursiveTaskExecutorException the recursive task executor
   *           exception
   */
  public <T extends RecursiveTask> void executeSequentially(final Collection<T> tasks)
      throws RecursiveTaskExecutorException {
    if (tasks.isEmpty()) {
      return;
    }
    // direct sequential execution
    prepareTasks(tasks);
    for (final T task : tasks) {
      task.execute();
    }
  }

  /**
   * Gets the number of execution threads. The number is an information. In
   * reality, there could be more or less threads but this should be rare.
   * 
   * @return the number of execution threads
   */
  public int getThreadCount() {
    return threadCount;
  }

  /**
   * Parallel execute.
   * 
   * @param <T> the generic type
   * @param tasks the tasks
   */
  private <T extends RecursiveTask> void parallelExecute(final T[] tasks) {
    enqueTasks(tasks);

    while (!allDone(tasks)) {
      final RecursiveTask current;
      synchronized (taskList) {
        final int size = taskList.size();
        if (size > 0) {
          current = taskList.remove(size - 1);
        } else {
          current = null;
        }
      }
      if (current != null) {
        current.execute();
      } else {
        synchronized (notificationObject) {
          try {
            notificationObject.wait(RecursiveTaskExecutor.NOTITY_WAIT);
          } catch (final InterruptedException e) {
            logger.warn("Execution interrupted");
          }
        }
      }
    }
  }

  private <T extends RecursiveTask> int prepareTasks(final Collection<T> tasks) {
    final RecursiveTask parentTask;
    final String threadName;
    final int depth;

    final Thread currentThread = Thread.currentThread();
    final Deque<RecursiveTask> callStack = threadsCallStacks.getThreadCallStack(currentThread);
    parentTask = callStack.peekLast();
    if (parentTask != null) {
      threadName = parentTask.threadName;
    } else {
      threadName = currentThread.getName();
    }

    depth = callStack.size() + 1;

    for (final T task : tasks) {
      task.prepareExecution(notificationObject, threadsCallStacks, threadName, depth);
    }
    return depth;
  }

  private <T extends RecursiveTask> int prepareTasks(final T[] tasks) {
    final RecursiveTask parentTask;
    final String threadName;
    final int depth;

    final Thread currentThread = Thread.currentThread();
    final Deque<RecursiveTask> callStack = threadsCallStacks.getThreadCallStack(currentThread);
    parentTask = callStack.peekLast();
    if (parentTask != null) {
      threadName = parentTask.threadName;
    } else {
      threadName = currentThread.getName();
    }

    depth = callStack.size() + 1;

    for (int i = tasks.length - 1; i >= 0; --i) {
      tasks[i].prepareExecution(notificationObject, threadsCallStacks, threadName, depth);
    }
    return depth;
  }

  // blocking
  /**
   * Take task.
   * 
   * @return the recursive task
   * @throws InterruptedException the interrupted exception
   */
  RecursiveTask takeTask() throws InterruptedException {
    while (true) {
      final RecursiveTask task;
      synchronized (taskList) {
        final int size = taskList.size();
        if (size > 0) {
          task = taskList.remove(size - 1);
        } else {
          task = null;
        }
      }
      if (task != null) {
        return task;
      } else {
        synchronized (notificationObject) {
          notificationObject.wait(RecursiveTaskExecutor.NOTITY_WAIT);
        }
      }
    }
  }
}
