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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.projectsforge.utils.tasksexecutor.RecursiveTaskExecutorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class for implementing the visitor pattern. To define visiting
 * methods, a series of methods with public or package visibility must be
 * defined. Suppose the class {@code Input} extends {@link TInput}, the class
 * 
 * @param <TInput> the type of input objects
 * @param <TState> the type of state objects
 * @param <TOutput> the type of output objects {@code State} extends
 *          {@link TState} and the class {@code Output} extends {@link TOutput}.
 *          <ul>
 *          <li>methods whose name starts with {@code preVisit} are called
 *          before the visiting of a node :
 *          <ul>
 *          <li>{@code void preVisit(Input input)}</li>
 *          <li>{@code void preVisit(Input input, State state)}: a state object
 *          is needed</li>
 *          </ul>
 *          </li>
 *          <li>methods whose name starts with {@code visit} are called to visit
 *          a node:
 *          <ul>
 *          <li>{@code void visit(Input input)}: {@link TOutput} is {@link Void}
 *          </li>
 *          <li>{@code void visit(Input input, State state)}: {@link TOutput} is
 *          {@link Void} and a state object is needed</li>
 *          <li>{@code Output visit(Input input)}</li>
 *          <li>{@code Output visit(Input input, State state)}: a state object
 *          is needed</li>
 *          </ul>
 *          </li>
 *          <li>methods whose name starts with {@code postVisit} are called
 *          after the visiting of a node
 *          <ul>
 *          <li>{@code void postVisit(Input input)}</li>
 *          <li>{@code void postVisit(Input input, Output output)}</li>
 *          <li>{@code void postVisit(Input input, Output output, State state)}:
 *          a state object is needed</li>
 *          </ul>
 *          </li>
 *          </ul>
 *          Statistics can be recorded while running in order to organize the
 *          call to the methods in the most efficient way :
 *          <ul>
 *          <li>Statistics are recorded by default in directory
 *          ${user.home}/.projectsforge/utils/visitors</li>
 *          <li>To change this directory, either the
 *          {@code org.projectsforge.utils.visitor.dir} system property is
 *          defined or {@link #setStatisticsDirectory(File)} is used</li>
 *          <li>To enable the profiling of the calls, the
 *          {@code org.projectsforge.utils.visitor.enableProfiling} system
 *          property must be set to {@code true}</li>
 *          <li>Statistics must be saved by calling saveStatictics().</li>
 *          <li>The saved statistics are used on next start of the program</li>
 *          </ul>
 * @author Sébastien Aupetit
 */
@SuppressWarnings("javadoc")
public abstract class Visitor<TInput, TState, TOutput> {

  /** The {@link VisitorDelegator} descriptors. */
  private static Map<Class<?>, VisitorDelegatorDescriptor> visitorDelegatorDescriptors = new HashMap<>();

  /** The statistics directory. */
  private static File statisticsDirectory = new File(System.getProperty("org.projectsforge.utils.visitor.dir",
      System.getProperty("user.home") + File.separatorChar + ".projectsforge" + File.separatorChar + "utils"
          + File.separatorChar + "visitors"));

  public static final String VISITOR_STATISTICS_DIR_PROPERTY = "org.projectsforge.utils.visitor.dir";

  /** The Constant ENABLE_PROFILING_PROPERTY. */
  public static final String ENABLE_PROFILING_PROPERTY = "org.projectsforge.utils.visitor.enableProfiling";

  /**
   * Gets the descriptor.
   * 
   * @param visitorClass the visitor class
   * @return the descriptor
   */
  @SuppressWarnings("rawtypes")
  private static VisitorDelegatorDescriptor getDescriptor(final Class<? extends Visitor> visitorClass) {

    synchronized (Visitor.visitorDelegatorDescriptors) {
      VisitorDelegatorDescriptor descriptor = Visitor.visitorDelegatorDescriptors.get(visitorClass);
      if (descriptor == null) {
        descriptor = new VisitorDelegatorDescriptor(visitorClass);
        Visitor.visitorDelegatorDescriptors.put(visitorClass, descriptor);
      }
      return descriptor;
    }
  }

  /**
   * Gets the statistics directory.
   * 
   * @return the statistics directory
   */
  public static File getStatisticsDirectory() {
    return Visitor.statisticsDirectory;
  }

  /**
   * Checks if is profiling enabled.
   * 
   * @return true, if is profiling enabled
   */
  private static boolean isProfilingEnabled() {
    return Boolean.parseBoolean(System.getProperty(Visitor.ENABLE_PROFILING_PROPERTY, "false"));
  }

  public static synchronized void saveStatistics() {
    for (final VisitorDelegatorDescriptor descriptor : Visitor.visitorDelegatorDescriptors.values()) {
      descriptor.saveStatistics();
    }
  }

  /**
   * Sets the statistics directory.
   * 
   * @param statisticsDirectory the new statistics directory
   */
  public static void setStatisticsDirectory(final File statisticsDirectory) {
    Visitor.statisticsDirectory = statisticsDirectory;
  }

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** The default visiting mode. */
  private final VisitingMode defaultVisitingMode;

  /** The delegator. */
  private final VisitorDelegator delegator;

  /** The descriptor. */
  private final VisitorDelegatorDescriptor descriptor;

  /** The profiled. */
  private final boolean profiled;

  /**
   * Instantiates a new visitor.
   * 
   * @param defaultVisitingMode the default visiting mode
   */
  public Visitor(final VisitingMode defaultVisitingMode) {
    this.defaultVisitingMode = defaultVisitingMode;
    this.descriptor = Visitor.getDescriptor(getClass());
    this.profiled = Visitor.isProfilingEnabled();

    if (profiled) {
      this.delegator = descriptor.profiledDelegator;
    } else {
      this.delegator = descriptor.unprofiledDelegator;
    }
  }

  /**
   * Gets the default visiting mode.
   * 
   * @return the default visiting mode
   */
  public VisitingMode getDefaultVisitingMode() {
    return defaultVisitingMode;
  }

  /**
   * Gets the logger.
   * 
   * @return the logger
   */
  public Logger getLogger() {
    return logger;
  }

  /**
   * Recurse on a collection of objects.
   * 
   * @param objects the objects
   * @param state the state
   * @return the t output[]
   * @throws RecursiveTaskExecutorException
   */
  @SuppressWarnings("unchecked")
  public final TOutput[] recurse(final Collection<TInput> objects, final TState state)
      throws RecursiveTaskExecutorException {
    return (TOutput[]) delegator.recurseAll(this, defaultVisitingMode, objects, state);
  }

  /**
   * Recurse on an object.
   * 
   * @param object the object
   * @param state the state
   * @return the t output
   * @throws RecursiveTaskExecutorException
   */
  @SuppressWarnings("unchecked")
  public final TOutput recurse(final TInput object, final TState state) throws RecursiveTaskExecutorException {
    return (TOutput) delegator.recurse(this, object, state);
  }

  /**
   * Recurse on many objects.
   * 
   * @param objects the objects
   * @param state the state
   * @return the t output[]
   * @throws RecursiveTaskExecutorException
   */
  @SuppressWarnings("unchecked")
  public final TOutput[] recurse(final TInput[] objects, final TState state) throws RecursiveTaskExecutorException {
    return (TOutput[]) delegator.recurseAll(this, defaultVisitingMode, objects, state);
  }

  /**
   * Recurse on a collection of objects.
   * 
   * @param visitingMode the visiting mode
   * @param objects the objects
   * @param state the state
   * @return the t output[]
   * @throws RecursiveTaskExecutorException
   */
  @SuppressWarnings("unchecked")
  public final TOutput[] recurse(final VisitingMode visitingMode, final Collection<? extends TInput> objects,
      final TState state) throws RecursiveTaskExecutorException {
    return (TOutput[]) delegator.recurseAll(this, visitingMode, objects, state);
  }

  /**
   * Recurse on many objects.
   * 
   * @param visitingMode the visiting mode
   * @param objects the objects
   * @param state the state
   * @return the t output[]
   * @throws RecursiveTaskExecutorException
   */
  @SuppressWarnings("unchecked")
  public final TOutput[] recurse(final VisitingMode visitingMode, final TInput[] objects, final TState state)
      throws RecursiveTaskExecutorException {
    return (TOutput[]) delegator.recurseAll(this, visitingMode, objects, state);
  }
}
