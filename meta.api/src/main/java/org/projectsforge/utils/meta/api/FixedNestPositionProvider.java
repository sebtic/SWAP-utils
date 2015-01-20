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
 * An implementation of {@link NestPositionProvider} which provides a predefined
 * initial nest position.
 * 
 * @param <Model>
 *          the generic type
 */
public class FixedNestPositionProvider<Model> implements NestPositionProvider<Model> {

  /** The nest position. */
  private final ScoredModel<Model> nestPosition;

  /**
   * Instantiates a new fixed nest position provider.
   * 
   * @param nestPosition
   *          the nest position
   */
  public FixedNestPositionProvider(final ScoredModel<Model> nestPosition) {
    this.nestPosition = nestPosition;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.projectsforge.utils.meta.api.NestPositionProvider#getNestPosition(org
   * .projectsforge.utils.meta.api.API)
   */
  @Override
  public ScoredModel<Model> getNestPosition(final API<Model> api) {
    return nestPosition;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FixedNestPositionProvider[nestPosition=" + nestPosition + "]";
  }
}
