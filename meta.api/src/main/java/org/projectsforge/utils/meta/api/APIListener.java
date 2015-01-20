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
 * The listener interface for receiving API events. The class that is interested
 * in processing a API event implements this interface, and the object created
 * with that class is registered with a component using the component's
 * <code>addAPIListener<code> method. When
 * the API event occurs, that object's appropriate
 * method is invoked.
 * 
 * @param <Model>
 *          the generic type
 */
public interface APIListener<Model> {

  /**
   * Best scored model improved.
   * 
   * @param api
   *          the api
   */
  public void bestScoredModelImproved(API<Model> api);

  /**
   * Explored solutions.
   * 
   * @param api
   *          the api
   * @param solutions
   *          the solutions
   */
  public void exploredSolutions(API<Model> api, ScoredModel<Model>[] solutions);

  /**
   * Iteration started.
   * 
   * @param api
   *          the api
   */
  public void iterationStarted(API<Model> api);

  /**
   * Nest moved.
   * 
   * @param api
   *          the api
   */
  public void nestMoved(API<Model> api);

}
