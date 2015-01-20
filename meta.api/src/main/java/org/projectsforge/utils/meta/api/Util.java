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
 * An utility class.
 */
public final class Util {

  /**
   * Gets the strategy.
   * 
   * @param nbValues
   *          the nb values
   * @param minValue
   *          the min value
   * @param maxValue
   *          the max value
   * @param strategy
   *          the strategy
   * @return the strategy
   */
  public static double[] getStrategy(final int nbValues, final double minValue,
      final double maxValue, final double strategy) {
    if (nbValues <= 0) {
      throw new IllegalArgumentException("nbValues must be strictly positive");
    }
    if (strategy <= 0) {
      throw new IllegalArgumentException("stategy must be strictly positive");
    }

    final double[] values = new double[nbValues];
    double a;
    double b;
    final boolean isLinear = Math.abs(strategy - 1.) <= 1.0e-30;

    if (isLinear) {
      b = (minValue - maxValue) / (1. - nbValues);
      a = maxValue - b * nbValues;
    } else {
      b = (maxValue - minValue) / (1. - Math.pow(strategy, 1. / nbValues - 1.));
      a = maxValue - b;
    }

    for (int i = 0; i < nbValues; ++i) {
      if (isLinear) {
        values[i] = a + b * (i + 1.);
      } else {
        values[i] = a + b * Math.pow(strategy, (i + 1.) / nbValues - 1.);
      }
      if (minValue < maxValue) {
        values[i] = Math.max(minValue, Math.min(maxValue, values[i]));
      } else {
        values[i] = Math.max(maxValue, Math.min(minValue, values[i]));
      }
    }
    return values;
  }

  /**
   * Gets the strategy.
   * 
   * @param nbValues
   *          the nb values
   * @param minValue
   *          the min value
   * @param maxValue
   *          the max value
   * @param strategy
   *          the strategy
   * @return the strategy
   */
  public static int[] getStrategy(final int nbValues, final int minValue, final int maxValue,
      final double strategy) {
    if (nbValues <= 0) {
      throw new IllegalArgumentException("nbValues must be strictly positive");
    }
    if (strategy <= 0) {
      throw new IllegalArgumentException("stategy must be strictly positive");
    }

    final int[] values = new int[nbValues];
    double a;
    double b;
    final boolean isLinear = Math.abs(strategy - 1.) <= 1.0e-30;

    if (isLinear) {
      b = (minValue - maxValue) / (1. - nbValues);
      a = maxValue - b * nbValues;
    } else {
      b = (maxValue - minValue) / (1. - Math.pow(strategy, 1. / nbValues - 1.));
      a = maxValue - b;
    }

    for (int i = 0; i < nbValues; ++i) {
      if (isLinear) {
        values[i] = (int) (a + b * (i + 1.));
      } else {
        values[i] = (int) (a + b * Math.pow(strategy, (i + 1.) / nbValues - 1.));
      }
      if (minValue < maxValue) {
        values[i] = Math.max(minValue, Math.min(maxValue, values[i]));
      } else {
        values[i] = Math.max(maxValue, Math.min(minValue, values[i]));
      }
    }
    return values;
  }

  /**
   * Private constructor forbidding instantiation.
   */
  private Util() {
    // nothing to do
  }
}
