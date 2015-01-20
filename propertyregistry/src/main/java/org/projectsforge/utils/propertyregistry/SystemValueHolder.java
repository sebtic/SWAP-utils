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

import java.util.Objects;

public class SystemValueHolder implements ValueHolder {

  public static final String prefix = "systemproperty.";

  private final String name;

  private String defaultValue;

  public SystemValueHolder(final String name) {
    this.name = name;
  }

  @Override
  public synchronized String get() {
    return System.getProperty(name, defaultValue);
  }

  @Override
  public String getDefault() {
    return defaultValue;
  }

  @Override
  public String getName() {
    return SystemValueHolder.prefix + name;
  }

  @Override
  public synchronized boolean isDefault() {
    return Objects.equals(defaultValue, get());
  }

  @Override
  public synchronized void set(final String value) {
    System.setProperty(name, value);
  }

  @Override
  public void setDefault(final String defaultValue) {
    this.defaultValue = defaultValue;
    if (defaultValue != null) {
      System.setProperty(name, defaultValue);
    }
  }

}
