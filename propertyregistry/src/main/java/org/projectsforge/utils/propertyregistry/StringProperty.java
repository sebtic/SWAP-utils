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

public class StringProperty extends Property<String> {

  public StringProperty(final String name, final String defaultValue) {
    super(name);
    setDefaultValue(serialize(defaultValue));
  }

  public StringProperty(final ValueHolder valueHolder, final String defaultValue) {
    super(valueHolder, false);
    setDefaultValue(defaultValue);
  }

  @Override
  protected String deserialize(final String value) {
    return value;
  }

  @Override
  protected String serialize(final String value) {
    return value;
  }

}
