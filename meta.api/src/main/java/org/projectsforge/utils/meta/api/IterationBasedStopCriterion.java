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

/**
 * An implementation of {@link StopCriterion} which provides a fixed iteration
 * stop criterion.
 * 
 * @param <Model>
 *          the generic type
 */
public class IterationBasedStopCriterion<Model> implements StopCriterion<Model> {

  /** The max number of iterations. */
  private int maxNumberOfIterations;

  /**
   * Instantiates a new iteration based stop criterion.
   */
  public IterationBasedStopCriterion() {
  }

  /**
   * Instantiates a new iteration based stop criterion.
   * 
   * @param maxNumberOfIterations
   *          the max number of iterations
   */
  public IterationBasedStopCriterion(final int maxNumberOfIterations) {
    setMaxNumberOfIterations(maxNumberOfIterations);
  }

  /**
   * Gets the max number of iterations.
   * 
   * @return the max number of iterations
   */
  public int getMaxNumberOfIterations() {
    return maxNumberOfIterations;
  }

  /**
   * Sets the max number of iterations.
   * 
   * @param maxNumberOfIterations
   *          the new max number of iterations
   */
  public void setMaxNumberOfIterations(final int maxNumberOfIterations) {
    if (maxNumberOfIterations < 0) {
      throw new IllegalArgumentException("The parameter must be at least positive");
    }
    this.maxNumberOfIterations = maxNumberOfIterations;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.projectsforge.utils.meta.api.StopCriterion#shouldStop(org.projectsforge
   * .utils.meta.api.API)
   */
  @Override
  public boolean shouldStop(final API<Model> api) {
    return api.getCurrentIteration() >= getMaxNumberOfIterations();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "IterationBasedStopCriterion[maxNumberOfIterations=" + maxNumberOfIterations + "]";
  }
}
