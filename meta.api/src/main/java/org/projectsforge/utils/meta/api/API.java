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
package org.projectsforge.utils.meta.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.projectsforge.utils.events.EventListenerList;
import org.projectsforge.utils.tasksexecutor.RecursiveTaskExecutorException;
import org.projectsforge.utils.tasksexecutor.RecursiveTaskExecutorFactory;
import org.projectsforge.utils.tasksexecutor.RecursiveTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class API.
 * 
 * @param <Model> the generic type
 * @author Sébastien Aupetit
 */
public class API<Model> {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(API.class);

  /** The current iteration number. */
  private final AtomicInteger currentIteration = new AtomicInteger();

  /** The parameters. */
  private APIParameters<Model> parameters = null;

  /** The list of event listeners. */
  private final EventListenerList<APIListener<Model>> apiListeners = new EventListenerList<APIListener<Model>>();

  /** The ants. */
  private List<Ant<Model>> ants;

  /** The nest position. */
  private final AtomicReference<ScoredModel<Model>> nestPosition = new AtomicReference<>();

  /** The best scored model. */
  private final AtomicReference<ScoredModel<Model>> bestScoredModel = new AtomicReference<>();

  /**
   * Adds the listener.
   * 
   * @param listener the listener
   */
  public void addAPIListener(final APIListener<Model> listener) {
    apiListeners.add(listener);
  }

  /**
   * Gets the ants.
   * 
   * @return the ants
   */
  public List<Ant<Model>> getAnts() {
    return ants;
  }

  /**
   * Gets the best scored model.
   * 
   * @return the best scored model
   */
  public ScoredModel<Model> getBestScoredModel() {
    return bestScoredModel.get();
  }

  /**
   * Gets the current iteration number.
   * 
   * @return the current iteration number
   */
  public int getCurrentIteration() {
    return currentIteration.get();
  }

  /**
   * Gets the parameters.
   * 
   * @return the parameters
   */
  public APIParameters<Model> getParameters() {
    return parameters;
  }

  /**
   * Register scored model.
   * 
   * @param scoredModel the scored model
   * @return true, if successful
   */
  @SuppressWarnings("unchecked")
  private boolean registerScoredModel(final ScoredModel<Model> scoredModel) {
    boolean improved = false;
    if (bestScoredModel.get() == null) {
      improved = true;
    } else {
      final double bestScore = bestScoredModel.get().getScore();
      if (parameters.isMaximize()) {
        if (bestScore < scoredModel.getScore()) {
          improved = true;
        }
      } else {
        if (bestScore > scoredModel.getScore()) {
          improved = true;
        }
      }
    }

    if (improved) {
      bestScoredModel.set(scoredModel);

      for (final Object listener : apiListeners.getListenerList()) {
        ((APIListener<Model>) listener).bestScoredModelImproved(this);
      }
    }

    return improved;
  }

  /**
   * Removes the api listener.
   * 
   * @param listener the listener
   */
  public void removeAPIListener(final APIListener<Model> listener) {
    apiListeners.remove(listener);
  }

  /**
   * Run.
   * 
   * @throws RecursiveTaskExecutorException
   */
  @SuppressWarnings("unchecked")
  public void run() throws RecursiveTaskExecutorException {
    if (parameters == null) {
      throw new IllegalStateException("API parameters not defined. Use setParameters().");
    }

    API.logger.info("Running the API metaheuristic with parameters {}", parameters);

    final ScoredModel<Model>[] newPositions = new ScoredModel[parameters.getColonySize()];

    // Create the nest
    nestPosition.set(parameters.getNestPrositionProvider().getNestPosition(this));
    registerScoredModel(nestPosition.get());

    // Create the ants
    ants = parameters.createAnts();

    currentIteration.set(0);

    // while not finished
    while (!parameters.getStopCriterion().shouldStop(this)) {

      for (final Object listener : apiListeners.getListenerList()) {
        ((APIListener<Model>) listener).iterationStarted(this);
      }

      // shall we move the nest and reset ants ?
      if (currentIteration.get() % parameters.getNestPatience() == 0) {
        // we move the nest to the best ever found model
        nestPosition.set(bestScoredModel.get());
        // we reset the ants
        for (final Ant<Model> ant : ants) {
          ant.nextExplorationMustBeFromNest();
        }
        for (final Object listener : apiListeners.getListenerList()) {
          ((APIListener<Model>) listener).nestMoved(this);
        }
      }

      // we explore the search space from the current positions of ants'memory
      if (parameters.isParallelExploration()) {
        final ArrayList<RecursiveTask> tasks = new ArrayList<>();
        for (final Ant<Model> ant : ants) {
          final RecursiveTask task = new RecursiveTask() {
            @Override
            public void run() throws Exception {
              if (ant.isNextExplorationFromNest()) {
                newPositions[ant.getIndex()] = parameters.getExplorationOperator()
                    .exploreHuntingSiteFromNest(API.this, ant, nestPosition.get());
              } else {
                newPositions[ant.getIndex()] = parameters.getExplorationOperator()
                    .explorePositionFromHuntingSite(API.this, ant, ant.getPosition());
              }
            }
          };
          tasks.add(task);
        }
        RecursiveTaskExecutorFactory.getInstance().execute(tasks);
      } else {
        for (final Ant<Model> ant : ants) {
          if (ant.isNextExplorationFromNest()) {
            newPositions[ant.getIndex()] = parameters.getExplorationOperator()
                .exploreHuntingSiteFromNest(API.this, ant, nestPosition.get());
          } else {
            newPositions[ant.getIndex()] = parameters.getExplorationOperator()
                .explorePositionFromHuntingSite(API.this, ant, ant.getPosition());
          }
        }
      }

      for (final Object listener : apiListeners.getListenerList()) {
        ((APIListener<Model>) listener).exploredSolutions(this, newPositions);
      }

      // we record the new positions
      for (final Ant<Model> ant : ants) {
        // if we explore from nest, we memorize the new position as the memory
        if (ant.isNextExplorationFromNest()) {
          ant.setHuntingSite(newPositions[ant.getIndex()]);
        } else {
          // if we explore from the memory, we keep the best position
          if (parameters.isMaximize()) {
            if (newPositions[ant.getIndex()].getScore() > ant.getPosition().getScore()) {
              ant.localExplorationIsASuccess(newPositions[ant.getIndex()]);
            } else {
              ant.localExplorationIsAFailure(newPositions[ant.getIndex()]);
            }
          } else {
            if (newPositions[ant.getIndex()].getScore() < ant.getPosition().getScore()) {
              ant.localExplorationIsASuccess(newPositions[ant.getIndex()]);
            } else {
              ant.localExplorationIsAFailure(newPositions[ant.getIndex()]);
            }
          }
        }
        registerScoredModel(newPositions[ant.getIndex()]);
      }
      currentIteration.incrementAndGet();
    }
  }

  /**
   * Sets the parameters.
   * 
   * @param parameters the new parameters
   */
  public void setParameters(final APIParameters<Model> parameters) {
    this.parameters = parameters;
  }
}
