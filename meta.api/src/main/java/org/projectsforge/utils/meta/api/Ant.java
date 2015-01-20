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

import java.util.Map;
import java.util.TreeMap;

/**
 * The base class implementing the behavior of an ant.
 * 
 * @param <Model>
 *          the generic type
 */
public class Ant<Model> {

  /** The fail counter. */
  private int failCounter;

  /** The do next exploration from nest. */
  private boolean doNextExplorationFromNest = true;

  /** The local patience. */
  private int localPatience;

  /** The position. */
  private ScoredModel<Model> position;

  /** The index. */
  private final int index;

  /**
   * Instantiates a new ant.
   * 
   * @param index
   *          the index of the ant
   */
  public Ant(final int index) {
    this.index = index;
  }

  /**
   * Collect attributes.
   * 
   * @param attributes
   *          the attributes
   */
  public void collectAttributes(final Map<String, Object> attributes) {
    attributes.put("index", index);
    attributes.put("localPatience", localPatience);
    attributes.put("position", position);
    attributes.put("isExploringFromNest", doNextExplorationFromNest);
    attributes.put("failCounter", failCounter);
  }

  /**
   * Gets the fail counter.
   * 
   * @return the fail counter
   */
  public int getFailCounter() {
    return failCounter;
  }

  /**
   * Gets the index.
   * 
   * @return the index
   */
  public int getIndex() {
    return index;
  }

  /**
   * Gets the local patience.
   * 
   * @return the local patience
   */
  public int getLocalPatience() {
    return localPatience;
  }

  /**
   * Gets the position.
   * 
   * @return the position
   */
  public ScoredModel<Model> getPosition() {
    return position;
  }

  /**
   * Checks if is exploring from nest.
   * 
   * @return true, if is exploring from nest
   */
  public boolean isNextExplorationFromNest() {
    return doNextExplorationFromNest;
  }

  /**
   * Local exploration is a failure.
   * 
   * @param position
   *          the position
   */
  public void localExplorationIsAFailure(final ScoredModel<Model> position) {
    this.failCounter = getFailCounter() + 1;
    if (getFailCounter() >= getLocalPatience()) {
      doNextExplorationFromNest = true;
    }
  }

  /**
   * Local exploration is a success.
   * 
   * @param position
   *          the position
   */
  public void localExplorationIsASuccess(final ScoredModel<Model> position) {
    this.setPosition(position);
    this.failCounter = 0;
  }

  /**
   * Next exploration must be from nest.
   */
  public void nextExplorationMustBeFromNest() {
    doNextExplorationFromNest = true;
    this.failCounter = 0;
  }

  /**
   * Sets the hunting site.
   * 
   * @param position
   *          the new hunting site
   */
  public void setHuntingSite(final ScoredModel<Model> position) {
    this.setPosition(position);
    doNextExplorationFromNest = false;
    this.failCounter = 0;
  }

  /**
   * Sets the local patience.
   * 
   * @param localPatience
   *          the new local patience
   */
  public void setLocalPatience(final int localPatience) {
    this.localPatience = localPatience;
  }

  /**
   * Sets the position.
   * 
   * @param position
   *          the new position
   */
  public void setPosition(final ScoredModel<Model> position) {
    this.position = position;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final TreeMap<String, Object> attributes = new TreeMap<>();
    collectAttributes(attributes);
    return getClass() + "[" + attributes + "]";
  }
}
