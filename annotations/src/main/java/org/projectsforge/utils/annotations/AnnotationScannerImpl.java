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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.scannotation.AnnotationDB;
import org.scannotation.AnnotationDB.CrossReferenceException;

/**
 * This class allows the scanning of class annotations for auto configuration of
 * the project.
 * 
 * @author Sébastien Aupetit
 */
public final class AnnotationScannerImpl implements org.projectsforge.utils.annotations.AnnotationScanner {
  /** The logger. */
  private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AnnotationScannerImpl.class);

  /** The database of annotations. */
  private AnnotationDB db;

  @SuppressWarnings("unchecked")
  @Override
  public synchronized Map<String, Class<?>> getClasses(final int filter, final boolean includeIndirectAnnotations,
      final Class<?>[] annotations) {
    if (filter == 0) {
      throw new IllegalArgumentException("filter argument must not be null");
    }
    final Map<String, Class<?>> results = new TreeMap<>();
    for (final Class<?> annotation : annotations) {
      if (Annotation.class.isAssignableFrom(annotation)) {
        final Set<String> classes = db.getAnnotationIndex().get(annotation.getName());
        if (classes != null) {
          for (final String className : classes) {
            try {
              final Class<?> clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());

              if (!includeIndirectAnnotations && clazz.getAnnotation((Class<Annotation>) annotation) == null) {
                // annotation not on class but on super class/interface
                continue;
              }

              final int modifier = clazz.getModifiers();
              if (Modifier.isInterface(modifier)) {
                if ((filter & org.projectsforge.utils.annotations.AnnotationScanner.INCLUDE_INTERFACES) != 0) {
                  results.put(className, clazz);
                }
              } else if (Modifier.isAbstract(modifier)) {
                if ((filter & org.projectsforge.utils.annotations.AnnotationScanner.INCLUDE_ABSTRACT_CLASSES) != 0) {
                  results.put(className, clazz);
                }
              } else if (clazz.isAnnotation()) {
                if ((filter & org.projectsforge.utils.annotations.AnnotationScanner.INCLUDE_ANNOTATIONS) != 0) {
                  results.put(className, clazz);
                }
              } else {
                results.put(className, clazz);
              }

            } catch (final ClassNotFoundException e) {
              logger.warn("Class not found.", e);
            }

          }
        }
      } else {
        logger.warn("Class {} is not an annotation", annotation.getName());
      }
    }
    return results;
  }

  /**
   * Scan class annotations.
   * 
   * @param paths the paths
   * @param uriSearcher the uri searcher
   * @param ignoredPackages the ignored packages
   */
  public synchronized void scan(final Set<URL> paths, final Set<String> ignoredPackages) {
    if (db != null) {
      throw new IllegalStateException("Scanning of annotations has already be executed");
    }

    final Set<URL> correctedPaths = new HashSet<>();
    for (final URL path : paths) {
      if (!path.getPath().toLowerCase().endsWith(".jar")) {
        // ensure there is a slash at the end
        if (!path.getPath().endsWith("/")) {
          try {
            final URL url = new URL(path.toString() + "/");
            try (final InputStream conn = path.openStream()) {
              correctedPaths.add(url);
            } catch (final IOException e) {
              // nothing to do
            }
          } catch (final MalformedURLException e) {
            logger.warn("URL {} can not be corrected", e);
          }
        }
      } else {
        try (final InputStream conn = path.openStream()) {
          correctedPaths.add(path);
        } catch (final IOException e) {
          // nothing to do
        }
      }
    }

    // TODO : http://code.google.com/p/reflections/
    db = new AnnotationDB();

    try {

      db.setScanParameterAnnotations(false);
      db.setScanFieldAnnotations(false);
      db.setScanMethodAnnotations(false);
      db.addIgnoredPackages(ignoredPackages.toArray(new String[0]));
      db.scanArchives(correctedPaths.toArray(new URL[0]));

      try {
        db.crossReferenceMetaAnnotations();
      } catch (final CrossReferenceException e) {
        logger.debug("Missing implemented interface : {}", e.getUnresolved());
      }
      try {
        db.crossReferenceImplementedInterfaces();
      } catch (final CrossReferenceException e) {
        logger.debug("Missing implemented interface : {}", e.getUnresolved());
      }

    } catch (final IOException e) {
      logger.debug("Error while scanning for annotations", e);
      throw new IllegalStateException("Error while scanning for annotations", e);
    }

    if (logger.isDebugEnabled()) {
      for (final Entry<String, Set<String>> entry : new TreeMap<String, Set<String>>(db.getAnnotationIndex())
          .entrySet()) {
        logger.debug("Detected classes for annotations {} : {}", entry.getKey(), entry.getValue());
      }
    }
  }
}
