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
import java.util.Map;

/**
 * The parameters for the new implementation of the API algorithm.
 * 
 * @param <Model>
 *          the generic type
 */
public class NewAPIParameters<Model> extends APIParameters<Model> {

  /** The patience on the inner ring. */
  private int innerPatience = 4;

  /** The patience on the outer ring. */
  private int outerPatience = 4;

  /** The patience strategy. */
  private double patienceStrategy = 100;

  /*
   * (non-Javadoc)
   * @see
   * org.projectsforge.utils.meta.api.APIParameters#collectAttributes(java.util
   * .Map)
   */
  @Override
  public void collectAttributes(final Map<String, Object> attributes) {
    super.collectAttributes(attributes);
    attributes.put("innerPatience", innerPatience);
    attributes.put("outerPatience", outerPatience);
    attributes.put("patienceStrategy", patienceStrategy);
  }

  /*
   * (non-Javadoc)
   * @see org.projectsforge.utils.meta.api.APIParameters#createAnts()
   */
  @Override
  public List<Ant<Model>> createAnts() {
    final List<Ant<Model>> ants = new ArrayList<>();

    final int[] patienceValues = Util.getStrategy(getColonySize(), getInnerPatience(),
        getOuterPatience(), getPatienceStrategy());
    final double[] amplitudesLimits = Util.getStrategy(getColonySize(), getMinAmplitude(),
        getMaxAmplitude(), getAmplitudeStrategy());

    for (int i = 0; i < getColonySize(); ++i) {
      final NewAnt<Model> ant = new NewAnt<>(i);
      ant.setLocalPatience(patienceValues[i]);
      ant.setMaxAmplitude(amplitudesLimits[i]);
      if (i == 0) {
        ant.setMinAmplitude(0);
      } else {
        ant.setMinAmplitude(amplitudesLimits[i - 1]);
      }
      ants.add(ant);
    }
    return ants;
  }

  /**
   * Gets the patience on the inner ring.
   * 
   * @return the patience on the inner ring
   */
  public int getInnerPatience() {
    return innerPatience;
  }

  /**
   * Gets the patience on the outer ring.
   * 
   * @return the patience on the outer ring
   */
  public int getOuterPatience() {
    return outerPatience;
  }

  /**
   * Gets the patience strategy.
   * 
   * @return the patience strategy
   */
  public double getPatienceStrategy() {
    return patienceStrategy;
  }

  /**
   * Sets the patience on the inner ring.
   * 
   * @param innerPatience
   *          the new patience on the inner ring
   */
  public void setInnerPatience(final int innerPatience) {
    this.innerPatience = innerPatience;
  }

  /**
   * Sets the patience on the outer ring.
   * 
   * @param outerPatience
   *          the new patience on the outer ring
   */
  public void setOuterPatience(final int outerPatience) {
    this.outerPatience = outerPatience;
  }

  /**
   * Sets the patience strategy.
   * 
   * @param patienceStrategy
   *          the new patience strategy
   */
  public void setPatienceStrategy(final double patienceStrategy) {
    this.patienceStrategy = patienceStrategy;
  }

}
