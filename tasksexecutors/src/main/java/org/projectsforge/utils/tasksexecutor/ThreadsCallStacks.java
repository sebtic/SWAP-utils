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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Object storing task callstacks by thread using week reference on thread. <br>
 * The methods do not require to be fully thread safe. Only the Map level need
 * it.
 * 
 * @author seb
 */
public class ThreadsCallStacks {
  private final Map<Thread, Deque<RecursiveTask>> callStacks = new WeakHashMap<>();

  public synchronized Deque<RecursiveTask> getThreadCallStack(final Thread thread) {
    Deque<RecursiveTask> callStack = callStacks.get(thread);
    if (callStack == null) {
      callStack = new ArrayDeque<>();
      callStacks.put(thread, callStack);
    }
    return callStack;
  }
}
