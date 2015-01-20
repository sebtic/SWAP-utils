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
 * The new ant behavior.
 * 
 * @param <Model>
 *          the generic type
 */
public class NewAnt<Model> extends Ant<Model> {

  /** The min amplitude. */
  private double minAmplitude;

  /** The max amplitude. */
  private double maxAmplitude;

  /**
   * Instantiates a new new ant.
   * 
   * @param index
   *          the index
   */
  public NewAnt(final int index) {
    super(index);
  }

  /*
   * (non-Javadoc)
   * @see org.projectsforge.utils.meta.api.Ant#collectAttributes(java.util.Map)
   */
  @Override
  public void collectAttributes(final Map<String, Object> attributes) {
    super.collectAttributes(attributes);
    attributes.put("minAmplitude", minAmplitude);
    attributes.put("maxAmplitude", maxAmplitude);
  }

  /**
   * Gets the max amplitude.
   * 
   * @return the max amplitude
   */
  public double getMaxAmplitude() {
    return maxAmplitude;
  }

  /**
   * Gets the min amplitude.
   * 
   * @return the min amplitude
   */
  public double getMinAmplitude() {
    return minAmplitude;
  }

  /**
   * Sets the max amplitude.
   * 
   * @param maxAmplitude
   *          the new max amplitude
   */
  public void setMaxAmplitude(final double maxAmplitude) {
    this.maxAmplitude = maxAmplitude;
  }

  /**
   * Sets the min amplitude.
   * 
   * @param minAmplitude
   *          the new min amplitude
   */
  public void setMinAmplitude(final double minAmplitude) {
    this.minAmplitude = minAmplitude;
  }
}
