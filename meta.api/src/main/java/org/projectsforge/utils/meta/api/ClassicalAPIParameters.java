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
 * The parameters for the classical implementation of the API algorithm.
 * 
 * @param <Model>
 *          the generic type
 */
public class ClassicalAPIParameters<Model> extends APIParameters<Model> {

  /** The local patience. */
  private int localPatience;

  /** The factor used to compute local amplitudes from nest amplitude. */
  private double nestToLocalAmplitudeFactor;

  /**
   * Instantiates a new classical API parameters with some defaults parameters.
   * 
   * <pre>
   * setAmplitudeStrategy(100);
   * setMinAmplitude(0.01);
   * setMaxAmplitude(1.0);
   * setColonySize(20);
   * setNestPatience(8);
   * setMaximize(true);
   * setParallelExploration(false);
   * setLocalPatience(4);
   * setNestToLocalAmplitudeFactor(0.1);
   * </pre>
   * 
   */
  public ClassicalAPIParameters() {
    setAmplitudeStrategy(100);
    setMinAmplitude(0.01);
    setMaxAmplitude(1.0);
    setColonySize(20);
    setNestPatience(8);
    setMaximize(true);
    setParallelExploration(false);
    setLocalPatience(4);
    setNestToLocalAmplitudeFactor(0.1);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.projectsforge.utils.meta.api.APIParameters#collectAttributes(java.util
   * .Map)
   */
  @Override
  public void collectAttributes(final Map<String, Object> attributes) {
    super.collectAttributes(attributes);
    attributes.put("localPatience", localPatience);
    attributes.put("nestToLocalAmplitudeFactor", nestToLocalAmplitudeFactor);
  }

  /*
   * (non-Javadoc)
   * @see org.projectsforge.utils.meta.api.APIParameters#createAnts()
   */
  @Override
  public List<Ant<Model>> createAnts() {
    final List<Ant<Model>> ants = new ArrayList<>();

    double lnestAmplitude;
    double llocalAmplitude;

    final double[] values = Util.getStrategy(getColonySize(), getMinAmplitude(), getMaxAmplitude(),
        getAmplitudeStrategy());

    for (int i = 0; i < getColonySize(); ++i) {
      final ClassicalAnt<Model> ant = new ClassicalAnt<Model>(i);

      lnestAmplitude = values[i];
      llocalAmplitude = lnestAmplitude * getNestToLocalAmplitudeFactor();

      ant.setLocalAmplitude(llocalAmplitude);
      ant.setNestAmplitude(lnestAmplitude);
      ant.setLocalPatience(getLocalPatience());
      ants.add(ant);
    }
    return ants;
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
   * Gets the factor used to compute local amplitudes from nest amplitude.
   * 
   * @return the factor used to compute local amplitudes from nest amplitude
   */
  public double getNestToLocalAmplitudeFactor() {
    return nestToLocalAmplitudeFactor;
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
   * Sets the factor used to compute local amplitudes from nest amplitude.
   * 
   * @param nestToLocalAmplitudeFactor
   *          the new factor used to compute local amplitudes from nest
   *          amplitude
   */
  public void setNestToLocalAmplitudeFactor(final double nestToLocalAmplitudeFactor) {
    this.nestToLocalAmplitudeFactor = nestToLocalAmplitudeFactor;
  }

}
