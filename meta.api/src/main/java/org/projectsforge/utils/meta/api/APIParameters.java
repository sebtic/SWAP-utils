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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The base class holding API parameters.
 * 
 * @param <Model>
 *          the generic type
 */
public abstract class APIParameters<Model> {

  /**
   * The strategy used for the search of ants. Values greater than 1 makes ants
   * search more on near position whereas values lower than 1 makes ants search
   * on far position. Historical value is 100.
   */
  private double amplitudeStrategy;

  /** The max amplitude. */
  private double maxAmplitude;

  /** The min amplitude. */
  private double minAmplitude;

  /** The colony size. */
  private int colonySize;

  /** The nest patience. */
  private int nestPatience;

  /** The maximize. */
  private boolean maximize;

  /** The stop criterion. */
  private StopCriterion<Model> stopCriterion;

  /** The parallel exploration. */
  private boolean parallelExploration;

  /** The exploration operator. */
  private ExplorationOperator<Model> explorationOperator;

  /** The nest prosition provider. */
  private NestPositionProvider<Model> nestPrositionProvider;

  /**
   * Collect attributes.
   * 
   * @param attributes
   *          the attributes
   */
  public void collectAttributes(final Map<String, Object> attributes) {

    attributes.put("amplitudeStrategy", amplitudeStrategy);
    attributes.put("maxAmplitude", maxAmplitude);
    attributes.put("minAmplitude", minAmplitude);

    attributes.put("colonySize", colonySize);
    attributes.put("nestPatience", nestPatience);
    attributes.put("maximize", maximize);
    attributes.put("parallelExploration", parallelExploration);

    attributes.put("stopCriterion", stopCriterion);
    attributes.put("nestPrositionProvider", nestPrositionProvider);
    attributes.put("explorationOperator", explorationOperator);

  }

  /**
   * Creates the ants.
   * 
   * @return the list
   */
  public abstract List<Ant<Model>> createAnts();

  /**
   * Gets the strategy used for the search of ants.
   * 
   * @return the strategy used for the search of ants
   */
  public double getAmplitudeStrategy() {
    return amplitudeStrategy;
  }

  /**
   * Gets the colony size.
   * 
   * @return the colony size
   */
  public int getColonySize() {
    return colonySize;
  }

  /**
   * Gets the exploration operator.
   * 
   * @return the exploration operator
   */
  public ExplorationOperator<Model> getExplorationOperator() {
    return explorationOperator;
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
   * Gets the nest patience.
   * 
   * @return the nest patience
   */
  public int getNestPatience() {
    return nestPatience;
  }

  /**
   * Gets the nest prosition provider.
   * 
   * @return the nest prosition provider
   */
  public NestPositionProvider<Model> getNestPrositionProvider() {
    return nestPrositionProvider;
  }

  /**
   * Gets the stop criterion.
   * 
   * @return the stop criterion
   */
  public StopCriterion<Model> getStopCriterion() {
    return stopCriterion;
  }

  /**
   * Checks if is maximize.
   * 
   * @return true, if is maximize
   */
  public boolean isMaximize() {
    return maximize;
  }

  /**
   * Checks if is parallel exploration.
   * 
   * @return true, if is parallel exploration
   */
  public boolean isParallelExploration() {
    return parallelExploration;
  }

  /**
   * Sets the strategy used for the search of ants.
   * 
   * @param amplitudeStrategy
   *          the new strategy used for the search of ants
   */
  public void setAmplitudeStrategy(final double amplitudeStrategy) {
    this.amplitudeStrategy = amplitudeStrategy;
  }

  /**
   * Sets the colony size.
   * 
   * @param colonySize
   *          the new colony size
   */
  public void setColonySize(final int colonySize) {
    this.colonySize = colonySize;
  }

  /**
   * Sets the exploration operator.
   * 
   * @param explorationOperator
   *          the new exploration operator
   */
  public void setExplorationOperator(final ExplorationOperator<Model> explorationOperator) {
    this.explorationOperator = explorationOperator;
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
   * Sets the maximize.
   * 
   * @param maximize
   *          the new maximize
   */
  public void setMaximize(final boolean maximize) {
    this.maximize = maximize;
  }

  /**
   * Sets the max iteration.
   * 
   * @param maxIteration
   *          the new max iteration
   */
  public void setMaxIteration(final int maxIteration) {
    setStopCriterion(new IterationBasedStopCriterion<Model>(maxIteration));
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

  /**
   * Sets the nest patience.
   * 
   * @param nestPatience
   *          the new nest patience
   */
  public void setNestPatience(final int nestPatience) {
    this.nestPatience = nestPatience;
  }

  /**
   * Sets the nest prosition provider.
   * 
   * @param nestPrositionProvider
   *          the new nest prosition provider
   */
  public void setNestPrositionProvider(final NestPositionProvider<Model> nestPrositionProvider) {
    this.nestPrositionProvider = nestPrositionProvider;
  }

  /**
   * Sets the parallel exploration.
   * 
   * @param parallelExploration
   *          the new parallel exploration
   */
  public void setParallelExploration(final boolean parallelExploration) {
    this.parallelExploration = parallelExploration;
  }

  /**
   * Sets the stop criterion.
   * 
   * @param stopCriterion
   *          the new stop criterion
   */
  public void setStopCriterion(final StopCriterion<Model> stopCriterion) {
    this.stopCriterion = stopCriterion;
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
