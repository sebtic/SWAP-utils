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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverridedValueHolder implements ValueHolder {
  Logger logger = LoggerFactory.getLogger(OverridedValueHolder.class);

  private final String overridedValue;

  private final ValueHolder valueHolder;

  public OverridedValueHolder(final ValueHolder valueHolder, final String overridedValue) {
    this.valueHolder = valueHolder;
    this.overridedValue = overridedValue;
  }

  @Override
  public String get() {
    return overridedValue;
  }

  @Override
  public String getDefault() {
    if (logger.isDebugEnabled()) {
      logger.debug("Default value ignored for overrided {}", getName());
    }
    return "";
  }

  @Override
  public String getName() {
    return valueHolder.getName();
  }

  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public void set(final String value) {
    if (logger.isWarnEnabled()) {
      logger.warn("Can not set overrided property {}. Keeping overrided value", getName());
    }
  }

  @Override
  public void setDefault(final String defaultValue) {
    if (logger.isDebugEnabled()) {
      logger.debug("Default value ignored for overrided {}", getName());
    }
  }
}
