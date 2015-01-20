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
package org.projectsforge.utils.icasestring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class to manage case insensitive string.
 */
public final class ICaseString implements Comparable<ICaseString> {

  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(ICaseString.class);

  /** The value. */
  private final String value;

  /** The lower cased value. */
  private final String lowerCasedValue;

  /**
   * The Constructor.
   * 
   * @param value the value
   */
  public ICaseString(final String value) {
    if (value == null) {
      throw new IllegalArgumentException("The value can not be null");
    }
    this.value = value;
    this.lowerCasedValue = value.toLowerCase();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final ICaseString other) {
    return lowerCasedValue.compareTo(other.lowerCasedValue);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ICaseString) {
      final ICaseString other = (ICaseString) obj;
      return lowerCasedValue.equals(other.lowerCasedValue);
    }
    if (obj instanceof String) {
      ICaseString.logger.warn(
          "equals called with a String ({}) instead of a CaseInsensitiveString on {}", obj, this);
      ICaseString.logger.warn("Current stack trace is:", new Exception());
      return lowerCasedValue.equalsIgnoreCase((String) obj);
    }
    return false;
  }

  /**
   * Equals ignore case.
   * 
   * @param value the value
   * @return true, if successful
   */
  public boolean equalsIgnoreCase(final String value) {
    return lowerCasedValue.equalsIgnoreCase(value);
  }

  /**
   * Gets the value.
   * 
   * @return the string
   */
  public String get() {
    return value;
  }

  /**
   * Gets the lower cased value.
   * 
   * @return the lower cased value
   */
  public String getLowerCasedValue() {
    return lowerCasedValue;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return lowerCasedValue.hashCode();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return value;
  }
}
