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

/**
 * The class implementing the threads executing the tasks.
 * 
 * @author Sébastien Aupetit
 */
class RecursiveExecutorThread extends Thread {

  /** The task executor associated with the thread. */
  private final RecursiveTaskExecutor taskExecutor;

  /** The base name of the thread. */
  private final String baseName;

  /**
   * Instantiates a new executor thread.
   * 
   * @param taskExecutor the task executor
   * @param threadGroup the thread group
   * @param threadId the thread id
   */
  public RecursiveExecutorThread(final RecursiveTaskExecutor taskExecutor, final ThreadGroup threadGroup,
      final int threadId) {
    super(threadGroup, "RecursiveExecutorThread-" + threadId);
    // dont block JVM stopping if only such thread remain
    setDaemon(true);
    this.taskExecutor = taskExecutor;
    this.baseName = getName();
    setName("Idle > " + baseName);
  }

  /**
   * Gets the base name of the thread.
   * 
   * @return the name
   */
  String getBaseName() {
    return baseName;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    try {
      while (!Thread.interrupted()) {
        try {
          final RecursiveTask task = taskExecutor.takeTask();
          task.execute();
        } catch (final InterruptedException e) {
          return;
        }
      }
    } finally {
      synchronized (taskExecutor) {
        taskExecutor.runningThread--;
      }
    }
  }
}
