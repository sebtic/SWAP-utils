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
 * The class holding a scored model.
 * 
 * @param <Model>
 *          the generic type
 */
public class ScoredModel<Model> {
  /** The model. */
  private final Model model;

  /** The score. */
  private final double score;

  /**
   * Instantiates a new scored model.
   * 
   * @param model
   *          the model
   * @param score
   *          the score
   */
  public ScoredModel(final Model model, final double score) {
    this.model = model;
    this.score = score;
  }

  /**
   * Gets the model.
   * 
   * @return the model
   */
  public Model getModel() {
    return model;
  }

  /**
   * Gets the score.
   * 
   * @return the score
   */
  public double getScore() {
    return score;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "(model=" + getModel() + ", score=" + getScore() + ")";
  }
}
