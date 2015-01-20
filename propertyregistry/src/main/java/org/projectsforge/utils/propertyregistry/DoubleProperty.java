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
package org.projectsforge.utils.propertyregistry;

public class DoubleProperty extends Property<Double> {

  public DoubleProperty(final String name, final Double defaultValue) {
    super(name);
    setDefaultValue(serialize(defaultValue));
  }

  public DoubleProperty(final ValueHolder valueHolder, final boolean allowNull) {
    super(valueHolder, allowNull);
  }

  @Override
  protected Double deserialize(final String value) {
    return Double.parseDouble(value);
  }

  @Override
  protected String serialize(final Double value) {
    return value.toString();
  }

}
