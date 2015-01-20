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
 * A factory for accessing the default TastExecutor instance.
 */
public class RecursiveTaskExecutorFactory {
  /** The Constant DEFAULTTHREADGROUP. */
  private static final ThreadGroup DEFAULTTHREADGROUP = new ThreadGroup(
      "RecursiveTaskExecutor threads");

  static {
    DEFAULTTHREADGROUP.setMaxPriority(Thread.NORM_PRIORITY - 1);
  }

  /** The thread count. */
  private static int defaultThreadCount = Math.max(
      0,
      Integer.parseInt(System.getProperty("org.projectsforge.utils.executorThreadCount",
          Integer.toString(Runtime.getRuntime().availableProcessors()))));

  /** The instance. */
  private static RecursiveTaskExecutor instance = null;

  /**
   * Gets the RecursiveTaskExecutor instance.
   * 
   * @return single instance of RecursiveTaskExecutorFactory
   */
  public static synchronized RecursiveTaskExecutor getInstance() {
    if (instance == null) {
      instance = new RecursiveTaskExecutor(DEFAULTTHREADGROUP, defaultThreadCount);
    }
    return instance;
  }

  /**
   * Sets the task executor.
   * 
   * @param instance the new task executor
   */
  public static synchronized void setTaskExecutor(final RecursiveTaskExecutor instance) {
    RecursiveTaskExecutorFactory.instance = instance;
  }
}
