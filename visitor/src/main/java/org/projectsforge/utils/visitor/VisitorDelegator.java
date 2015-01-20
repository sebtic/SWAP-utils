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
package org.projectsforge.utils.visitor;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import org.projectsforge.utils.tasksexecutor.RecursiveTaskExecutorException;
import org.projectsforge.utils.tasksexecutor.RecursiveTaskExecutorFactory;
import org.projectsforge.utils.tasksexecutor.RecursiveTask;

/**
 * An internal interface used by {@link Visitor} to reduce the cost of
 * reflection by replacing it by generated bytecode allowing JIT compiler
 * optimizations.
 * 
 * @author Sébastien Aupetit
 */
public abstract class VisitorDelegator {

  /** The preVisit counters. */
  protected AtomicInteger[] preVisitCounters = null;

  /** The visit counters. */
  protected AtomicInteger[] visitCounters = null;

  /** The postVisit counters. */
  protected AtomicInteger[] postVisitCounters = null;

  /** The output class. */
  protected Class<?> outputClass;

  /**
   * Instantiates a new visitor delegator.
   */
  public VisitorDelegator() {
  }

  /**
   * Recurse on an object.
   * 
   * @param visitor the visitor
   * @param object the object
   * @param state the state
   * @return the object
   */
  @SuppressWarnings("rawtypes")
  public abstract Object recurse(final Visitor visitor, final Object object, final Object state)
      throws RecursiveTaskExecutorException;

  /**
   * Recurse on a list of objects.
   * 
   * @param visitor the visitor
   * @param visitingMode the visiting mode
   * @param objects the objects
   * @param state the state
   * @return the object[]
   */
  @SuppressWarnings("rawtypes")
  public abstract Object[] recurseAll(final Visitor visitor, VisitingMode visitingMode,
      final Collection objects, final Object state) throws RecursiveTaskExecutorException;

  /**
   * Recurse on many objects.
   * 
   * @param visitor the visitor
   * @param visitingMode the visiting mode
   * @param objects the objects
   * @param state the state
   * @return the object[]
   */
  @SuppressWarnings("rawtypes")
  public abstract Object[] recurseAll(final Visitor visitor, VisitingMode visitingMode,
      final Object[] objects, final Object state) throws RecursiveTaskExecutorException;

  /**
   * Recurse parallel non void output.
   * 
   * @param visitor the visitor
   * @param objects the objects
   * @param state the state
   * @return the object[]
   * @throws RecursiveTaskExecutorException
   */
  @SuppressWarnings("rawtypes")
  public final Object[] recurseParallelNonVoidOutput(final Visitor visitor,
      final Collection objects, final Object state) throws RecursiveTaskExecutorException {
    final int size = objects.size();
    if (size == 1) {
      for (final Object object : objects) {
        final Object[] array = (Object[]) Array.newInstance(outputClass, 1);
        array[0] = recurse(visitor, object, state);
        return array;
      }
      throw new IllegalStateException("This code must not be reached.");
    } else {
      final RecursiveTask[] tasks = new RecursiveTask[size];
      final Object[] outputs = (Object[]) Array.newInstance(outputClass, size);
      int i = 0;
      for (final Object object : objects) {
        final int id = i;
        tasks[i] = new RecursiveTask() {
          @Override
          public void run() throws Exception {
            outputs[id] = recurse(visitor, object, state);
          }
        };
        i++;
      }
      RecursiveTaskExecutorFactory.getInstance().execute(tasks);
      return outputs;
    }
  }

  /**
   * Recurse parallel non void output.
   * 
   * @param visitor the visitor
   * @param objects the objects
   * @param state the state
   * @return the object[]
   * @throws RecursiveTaskExecutorException
   */
  @SuppressWarnings("rawtypes")
  public final Object[] recurseParallelNonVoidOutput(final Visitor visitor, final Object[] objects,
      final Object state) throws RecursiveTaskExecutorException {
    final int size = objects.length;
    if (size == 1) {
      final Object[] array = (Object[]) Array.newInstance(outputClass, 1);
      array[0] = recurse(visitor, objects[0], state);
      return array;
    } else {
      final RecursiveTask[] tasks = new RecursiveTask[size];
      final Object[] outputs = (Object[]) Array.newInstance(outputClass, size);
      for (int i = size - 1; i >= 0; --i) {
        final int id = i;
        tasks[i] = new RecursiveTask() {
          @Override
          public void run() throws RecursiveTaskExecutorException {
            outputs[id] = recurse(visitor, objects[id], state);
          }
        };
      }
      RecursiveTaskExecutorFactory.getInstance().execute(tasks);
      return outputs;
    }
  }

  /**
   * Recurse parallel void output.
   * 
   * @param visitor the visitor
   * @param objects the objects
   * @param state the state
   * @return the object[]
   * @throws RecursiveTaskExecutorException
   */
  @SuppressWarnings("rawtypes")
  public final Object[] recurseParallelVoidOutput(final Visitor visitor, final Collection objects,
      final Object state) throws RecursiveTaskExecutorException {
    final int size = objects.size();
    if (size == 1) {
      for (final Object object : objects) {
        final Object[] array = (Object[]) Array.newInstance(outputClass, 1);
        array[0] = recurse(visitor, object, state);
        return array;
      }
      return null;
    } else {
      final RecursiveTask[] tasks = new RecursiveTask[size];
      int i = 0;
      for (final Object object : objects) {
        tasks[i] = new RecursiveTask() {
          @Override
          public final void run() throws RecursiveTaskExecutorException {
            recurse(visitor, object, state);
          }
        };
        i++;
      }
      RecursiveTaskExecutorFactory.getInstance().execute(tasks);
      return null;
    }
  }

  /**
   * Recurse parallel void output.
   * 
   * @param visitor the visitor
   * @param objects the objects
   * @param state the state
   * @return the object[]
   * @throws RecursiveTaskExecutorException
   */
  @SuppressWarnings("rawtypes")
  public final Object[] recurseParallelVoidOutput(final Visitor visitor, final Object[] objects,
      final Object state) throws RecursiveTaskExecutorException {
    final int size = objects.length;
    if (size == 1) {
      recurse(visitor, objects[0], state);
      return null;
    } else {
      final RecursiveTask[] tasks = new RecursiveTask[size];
      for (int i = size - 1; i >= 0; --i) {
        final int id = i;
        tasks[i] = new RecursiveTask() {
          @Override
          public final void run() throws RecursiveTaskExecutorException {
            recurse(visitor, objects[id], state);
          }
        };
      }
      RecursiveTaskExecutorFactory.getInstance().execute(tasks);
      return null;
    }
  }

  /**
   * Recurse sequential non void output.
   * 
   * @param visitor the visitor
   * @param objects the objects
   * @param state the state
   * @return the object[]
   */
  @SuppressWarnings("rawtypes")
  public final Object[] recurseSequentialNonVoidOutput(final Visitor visitor,
      final Collection objects, final Object state) throws RecursiveTaskExecutorException {
    final int size = objects.size();
    final Object[] outputs = (Object[]) Array.newInstance(outputClass, size);
    int i = 0;
    for (final Object object : objects) {
      outputs[i] = recurse(visitor, object, state);
      i++;
    }
    return outputs;
  }

  /**
   * Recurse sequential non void output.
   * 
   * @param visitor the visitor
   * @param objects the objects
   * @param state the state
   * @return the object[]
   */
  @SuppressWarnings("rawtypes")
  public final Object[] recurseSequentialNonVoidOutput(final Visitor visitor,
      final Object[] objects, final Object state) throws RecursiveTaskExecutorException {
    final int size = objects.length;
    final Object[] outputs = (Object[]) Array.newInstance(outputClass, size);
    for (int i = 0; i < size; ++i) {
      outputs[i] = recurse(visitor, objects[i], state);
    }
    return outputs;
  }

  /**
   * Recurse sequential void output.
   * 
   * @param visitor the visitor
   * @param objects the objects
   * @param state the state
   * @return the object[]
   */
  @SuppressWarnings("rawtypes")
  public final Object[] recurseSequentialVoidOutput(final Visitor visitor,
      final Collection objects, final Object state) throws RecursiveTaskExecutorException {
    for (final Object object : objects) {
      recurse(visitor, object, state);
    }
    return null;
  }

  /**
   * Recurse sequential void output.
   * 
   * @param visitor the visitor
   * @param objects the objects
   * @param state the state
   * @return the object[]
   */
  @SuppressWarnings("rawtypes")
  public final Object[] recurseSequentialVoidOutput(final Visitor visitor, final Object[] objects,
      final Object state) throws RecursiveTaskExecutorException {
    for (final Object object : objects) {
      recurse(visitor, object, state);
    }
    return null;
  }

  /**
   * Sets the counters.
   * 
   * @param preVisitCounters the pre visit counters
   * @param visitCounters the visit counters
   * @param postVisitCounters the post visit counters
   */
  public void setCounters(final AtomicInteger[] preVisitCounters,
      final AtomicInteger[] visitCounters, final AtomicInteger[] postVisitCounters) {
    this.preVisitCounters = preVisitCounters;
    this.visitCounters = visitCounters;
    this.postVisitCounters = postVisitCounters;
  }

  /**
   * Sets the output class.
   * 
   * @param outputClass the new output class
   */
  public void setOutputClass(final Class<?> outputClass) {
    this.outputClass = outputClass;
  }
}