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

public class SetOnceValueHolder implements ValueHolder {

  private String value;

  private boolean defined;

  private final String name;

  public SetOnceValueHolder(final String name) {
    this.name = name;
    this.defined = false;
  }

  @Override
  public String get() {
    if (!defined) {
      throw new IllegalArgumentException("SetOnceValueHolder " + getName() + " never set");
    }
    return value;
  }

  @Override
  public String getDefault() {
    return get();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public void set(final String value) {
    if (!defined) {
      this.value = value;
      defined = true;
    } else if (!this.value.equals(value)) {
      throw new IllegalArgumentException("SetOnceValueHolder " + getName() + " has already been set to " + this.value
          + ". New value is " + value);
    }
  }

  @Override
  public void setDefault(final String defaultValue) {
  }

}
