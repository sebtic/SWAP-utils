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
 * The Interface ExplorationOperator defining the two exploration operators for
 * the API algorithm.
 * 
 * @param <Model>
 *          the generic type
 */
public interface ExplorationOperator<Model> {

  /**
   * Explore a hunting site from the nest.
   * 
   * @param api
   *          the API algorithm
   * @param ant
   *          the ant
   * @param nestPosition
   *          the nest position
   * @return the scored model
   */
  ScoredModel<Model> exploreHuntingSiteFromNest(API<Model> api, Ant<Model> ant,
      ScoredModel<Model> nestPosition);

  /**
   * Explore a position from a hunting site.
   * 
   * @param api
   *          the API algorithm
   * @param ant
   *          the ant
   * @param sitePosition
   *          the site position
   * @return the scored model
   */
  ScoredModel<Model> explorePositionFromHuntingSite(API<Model> api, Ant<Model> ant,
      ScoredModel<Model> sitePosition);
}
