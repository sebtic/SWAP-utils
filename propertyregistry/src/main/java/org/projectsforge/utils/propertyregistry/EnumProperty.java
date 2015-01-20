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

public class EnumProperty<T extends Enum<T>> extends Property<T> {

  private final Class<T> enumType;

  public EnumProperty(final String name, final Class<T> enumType, final T defaultValue) {
    super(name);
    if (enumType == null) {
      throw new IllegalArgumentException("enumType can not be null for property " + getName());
    }
    this.enumType = enumType;
    setDefaultValue(serialize(defaultValue));
  }

  public EnumProperty(final ValueHolder valueHolder, final Class<T> enumType, final T defaultValue) {
    super(valueHolder, false);
    if (enumType == null) {
      throw new IllegalArgumentException("enumType can not be null for property " + getName());
    }
    this.enumType = enumType;
    setDefaultValue(serialize(defaultValue));
  }

  @Override
  protected T deserialize(final String value) {
    return Enum.valueOf(enumType, value);

  }

  public Class<T> getEnumType() {
    return enumType;
  }

  @Override
  protected String serialize(final T value) {
    return value.toString();
  }

  @Override
  public String toString() {
    return super.toString() + "[" + enumType.getSimpleName() + "]";
  }
}
