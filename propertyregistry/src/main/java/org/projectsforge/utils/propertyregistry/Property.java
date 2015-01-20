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

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Property<T> {

  protected static final Logger logger = LoggerFactory.getLogger(Property.class);

  private ValueHolder valueHolder;

  private final boolean allowNull;

  private PropertyRegistry propertyregistry;

  public Property(final String name) {
    this(new DefaultValueHolder(name), false);
  }

  public Property(final ValueHolder valueHolder, final boolean allowNull) {
    this.valueHolder = valueHolder;
    this.allowNull = allowNull;
  }

  protected abstract T deserialize(String value);

  public T get() {
    return deserialize(valueHolder.get());
  }

  public String getDefaultValue() {
    return valueHolder.getDefault();
  }

  public String getHeldValue() {
    return valueHolder.get();
  }

  public String getName() {
    return valueHolder.getName();
  }

  public PropertyRegistry getPropertyRegistry() {
    return propertyregistry;
  }

  ValueHolder getValueHolder() {
    return valueHolder;
  }

  synchronized void load(final Properties properties) {
    final String value = properties.getProperty(valueHolder.getName());
    if (value != null) {
      valueHolder.set(value);
    }
  }

  synchronized void save(final Properties properties) {
    if (!valueHolder.isDefault()) {
      properties.put(valueHolder.getName(), serialize(get()));
    }
  }

  protected abstract String serialize(T value);

  public void set(final T value) {
    if (value == null && !allowNull) {
      throw new IllegalArgumentException("Property " + valueHolder.getName() + " can not be set to null");
    }
    valueHolder.set(serialize(value));

  }

  protected void setDefaultValue(final String defaultValue) {
    valueHolder.setDefault(defaultValue);
  }

  public void setHeldValue(final String value) {
    valueHolder.set(value);
  }

  void setPropertyregistry(final PropertyRegistry propertyregistry) {
    this.propertyregistry = propertyregistry;
  }

  void setValueHolder(final ValueHolder valueHolder) {
    this.valueHolder = valueHolder;
  }

  @Override
  public String toString() {
    return valueHolder.getName() + "=" + valueHolder.get();
  }

}
