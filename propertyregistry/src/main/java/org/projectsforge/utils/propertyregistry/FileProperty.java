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

import java.io.File;

public class FileProperty extends Property<File> {

  private boolean failOnNull;

  public FileProperty(final String name, final File defaultValue, final boolean failOnNull) {
    super(name);
    setDefaultValue(serialize(defaultValue));
    this.failOnNull = failOnNull;
  }

  public FileProperty(final ValueHolder valueHolder, final File defaultValue) {
    super(valueHolder, false);
    setDefaultValue(serialize(defaultValue));
  }

  @Override
  protected File deserialize(final String value) {
    if (value == null) {
      if (failOnNull) {
        throw new IllegalStateException("Property must not be null");
      } else {
        return null;
      }
    } else {
      return new File(value);
    }
  }

  @Override
  protected String serialize(final File value) {
    if (value == null) {
      return null;
    } else {
      return value.getAbsolutePath();
    }
  }

}
