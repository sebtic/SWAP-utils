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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The exception class raised if an exception is raised by at least a recursive
 * task.
 * 
 * @author Sébastien Aupetit
 */
public class RecursiveTaskExecutorException extends Exception {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 0;

  /** The tasks. */
  private final List<RecursiveTask> tasks = new ArrayList<>();

  /**
   * Instantiates a new recursive task executor exception.
   * 
   * @param <T> the generic type
   * @param tasks the tasks
   */
  public <T extends RecursiveTask> RecursiveTaskExecutorException(final Collection<T> tasks) {
    for (final T task : tasks) {
      if (task.getException() != null) {
        this.tasks.add(task);
      }
    }
  }

  /**
   * Instantiates a new recursive task executor exception.
   * 
   * @param <T> the generic type
   * @param tasks the tasks
   */
  public <T extends RecursiveTask> RecursiveTaskExecutorException(final T[] tasks) {
    for (int i = tasks.length - 1; i >= 0; --i) {
      if (tasks[i].getException() != null) {
        this.tasks.add(tasks[i]);
      }
    }
  }

  /**
   * Gets the tasks which raised an exception.
   * 
   * @return the tasks
   */
  public List<RecursiveTask> getTasks() {
    return tasks;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Throwable#toString()
   */
  @Override
  public String toString() {
    return "RecursiveTaskExecutorException [" + tasks + "]";
  }

}
