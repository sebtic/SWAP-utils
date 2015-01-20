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
package org.projectsforge.utils.annotations;

import java.util.Map;

/**
 * The interface which allows the scanning of class annotations for auto
 * configuration of the project.
 */
public interface AnnotationScanner {

  /** To include plain classes. */
  public final static int INCLUDE_PLAIN_CLASSES = 1;

  /** To include interfaces. */
  public final static int INCLUDE_INTERFACES = 2;

  /** To include abstract classes. */
  public final static int INCLUDE_ABSTRACT_CLASSES = 4;

  /** To include annotations. */
  public final static int INCLUDE_ANNOTATIONS = 8;

  /**
   * Gets the classes.
   * 
   * @param annotations the annotations
   * @param filter the filter: a OR combination of
   *          {@link #INCLUDE_PLAIN_CLASSES}, {@link #INCLUDE_INTERFACES},
   *          {@link #INCLUDE_ABSTRACT_CLASSES}, {@link #INCLUDE_ANNOTATIONS}.
   * @param includeIndirectAnnotations should we include class with annotations
   *          on superclass or superinterfaces ?
   * @return the classes
   */
  Map<String, Class<?>> getClasses(final int filter, boolean includeIndirectAnnotations,
      Class<?>[] annotations);

}
