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

/**
 * The classical ant behavior.
 * 
 * @param <Model>
 *          the generic type
 */
public class ClassicalAnt<Model> extends Ant<Model> {

  /** The local amplitude in the range [0;1]. */
  private double localAmplitude;

  /** The nest amplitude in the range [0;1]. */
  private double nestAmplitude;

  /**
   * Instantiates a new classical ant.
   * 
   * @param index
   *          the index
   */
  public ClassicalAnt(final int index) {
    super(index);
  }

  /*
   * (non-Javadoc)
   * @see org.projectsforge.utils.meta.api.Ant#collectAttributes(java.util.Map)
   */
  @Override
  public void collectAttributes(final Map<String, Object> attributes) {
    super.collectAttributes(attributes);
    attributes.put("localAmplitude", localAmplitude);
    attributes.put("nestAmplitude", nestAmplitude);
  }

  /**
   * Gets the local amplitude in the range [0;1].
   * 
   * @return the local amplitude in the range [0;1]
   */
  public double getLocalAmplitude() {
    return localAmplitude;
  }

  /**
   * Gets the nest amplitude in the range [0;1].
   * 
   * @return the nest amplitude in the range [0;1]
   */
  public double getNestAmplitude() {
    return nestAmplitude;
  }

  /**
   * Sets the local amplitude in the range [0;1].
   * 
   * @param localAmplitude
   *          the new local amplitude in the range [0;1]
   */
  public void setLocalAmplitude(final double localAmplitude) {
    this.localAmplitude = localAmplitude;
  }

  /**
   * Sets the nest amplitude in the range [0;1].
   * 
   * @param nestAmplitude
   *          the new nest amplitude in the range [0;1]
   */
  public void setNestAmplitude(final double nestAmplitude) {
    this.nestAmplitude = nestAmplitude;
  }
}
