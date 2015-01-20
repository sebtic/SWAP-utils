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

import java.util.Deque;

/**
 * The Interface RecursiveTask for implementing parallel independent recursive
 * tasks.
 * 
 * @author Sébastien Aupetit
 */
public abstract class RecursiveTask {

  /** The thread name. */
  String threadName;

  /** The exception. */
  private Exception exception;

  /** Indicate if the execution of the task is done. */
  volatile boolean executed = false;

  /** The depth in task hierarchy. */
  int depth;

  /** The threads call stacks. */
  private ThreadsCallStacks threadsCallStacks;

  /** The notification object. */
  private Object notificationObject;

  /**
   * Execute.
   */
  void execute() {
    try {
      final Thread currentThread = Thread.currentThread();
      final Deque<RecursiveTask> callStack = threadsCallStacks.getThreadCallStack(currentThread);
      // push the task on the callstack
      callStack.addLast(this);
      try {
        if (currentThread instanceof RecursiveExecutorThread) {
          final String currentThreadBaseName = ((RecursiveExecutorThread) currentThread)
              .getBaseName();
          ((RecursiveExecutorThread) currentThread).setName(threadName + " > "
              + currentThreadBaseName);
          try {
            run();
          } finally {
            ((RecursiveExecutorThread) currentThread).setName("Idle > " + currentThreadBaseName);
          }
        } else {
          run();
        }
      } finally {
        // pop the task from the callstack
        if (callStack.removeLast() != this) {
          throw new IllegalStateException("I removed a wrong task. It's a bug");
        }
      }
    } catch (final Exception e) {
      this.exception = e;
    } finally {
      executed = true;
      if (notificationObject != null) {
        synchronized (notificationObject) {
          notificationObject.notifyAll();
        }
      }
    }
  }

  /**
   * Gets the exception raised by the execution.
   * 
   * @return the exception or null if no exception has been raised
   */
  public Exception getException() {
    return exception;
  }

  /**
   * Prepare the execution (internal use only).
   * 
   * @param notificationObject the notification object
   * @param threadsCallStacks the threads call stacks
   * @param threadName the thread name
   * @param depth the depth
   */
  void prepareExecution(final Object notificationObject, final ThreadsCallStacks threadsCallStacks,
      final String threadName, final int depth) {
    this.notificationObject = notificationObject;
    this.threadsCallStacks = threadsCallStacks;
    this.threadName = threadName;
    this.depth = depth;
    exception = null;
    executed = false;
  }

  /**
   * Run the task.
   * 
   * @throws Exception the exception
   */
  protected abstract void run() throws Exception;

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "RecursiveTask [" + threadName + "(" + depth + "," + (executed ? "E" : "-") + " ex="
        + exception + "]";
  }

}
