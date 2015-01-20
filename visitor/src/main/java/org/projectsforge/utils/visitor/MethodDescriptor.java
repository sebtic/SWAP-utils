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
package org.projectsforge.utils.visitor;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class used to describes the properties of a method.
 * 
 * @author Sébastien Aupetit
 */
class MethodDescriptor {

  /** The class name of the first parameter. */
  public final String className;

  /** The first parameter class. */
  public final Class<?> parameterClass;

  /** The method. */
  public final Method method;

  /** The usage counter. */
  public final AtomicInteger counter;

  /**
   * Instantiates a new method descriptor.
   * 
   * @param parameterClass the parameter class
   * @param method the method
   */
  public MethodDescriptor(final Class<?> parameterClass, final Method method) {
    this.parameterClass = parameterClass;
    this.className = parameterClass.getName();
    this.counter = new AtomicInteger(0);
    this.method = method;
  }
}
