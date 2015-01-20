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
package org.projectsforge.utils.path;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The class managing a set of paths, path detectors and result paths.
 * 
 * @author Sébastien Aupetit
 */
public class PathRepository {

  /** The result paths. */
  private Set<URL> paths = new HashSet<URL>();

  /** The path detectors. */
  private final List<PathDetector> detectors = new ArrayList<PathDetector>();

  /** A boolean indicating if the detection has been executed. */
  private boolean executed = false;

  /** The logger. */
  private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PathRepository.class);

  /**
   * Adds a path to the repository.
   * 
   * @param path the path
   */
  public synchronized void addPath(final URL path) {
    if (executed) {
      throw new IllegalStateException("PathRepository already filled");
    }

    if (paths.add(path)) {
      logger.debug("Adding path: {}", path);
    }
  }

  /**
   * Adds a path detector.
   * 
   * @param detector the PathDetector
   */
  public synchronized void addPathDetector(final PathDetector detector) {
    if (executed) {
      throw new IllegalStateException("PathRepository already filled");
    }
    detectors.add(detector);
    executed = false;
  }

  /**
   * Adds a list of paths to the repository.
   * 
   * @param listOfPaths the list of paths
   */
  public synchronized void addPaths(final List<URL> listOfPaths) {
    if (executed) {
      throw new IllegalStateException("PathRepository already filled");
    }

    for (final URL path : listOfPaths) {
      addPath(path);
    }
  }

  /**
   * Gets the result paths.
   * <p>
   * Subsequent calls to the method return the same paths. The first call to
   * this method runs the path detection. After the detection, the paths can not
   * be modified.
   * 
   * @return the paths
   */
  public synchronized Set<URL> getPaths() {
    if (!executed) {
      paths = new HashSet<URL>();
      for (final PathDetector detector : detectors) {
        logger.debug("Detecting with {}", detector);
        detector.detect(this);
      }
      executed = true;
    }
    return paths;
  }
}
