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

/**
 * The factory which manages construction and sharing of PathRepository
 * instances.
 * 
 * @author Sébastien Aupetit
 */
public final class PathRepositoryFactory {

  /** The default path repository. */
  private static PathRepository defaultPathRepository;

  /**
   * Gets the default path repository instance.
   * 
   * @return the default path repository instance
   */
  public static PathRepository getDefaultPathRepository() {
    return PathRepositoryFactory.getDefaultPathRepository(true);
  }

  /**
   * Gets the default path repository instance.
   * 
   * @param useDefault true if the repository should be setup with paths derived
   *          from class paths, false otherwise
   * @return the default path repository instance Subsequent calls to this
   *         method return the same instance.
   */
  public static PathRepository getDefaultPathRepository(final boolean useDefault) {
    synchronized (PathRepositoryFactory.class) {
      if (PathRepositoryFactory.defaultPathRepository == null) {
        PathRepositoryFactory.defaultPathRepository = PathRepositoryFactory
            .getNewPathRepository(useDefault);
      }
      return PathRepositoryFactory.defaultPathRepository;
    }
  }

  /**
   * Gets a new path repository instance.
   * 
   * @param useDefault true if the repository should be setup with paths derived
   *          from class paths, false otherwise
   * @return the new path repository instance
   */
  public static PathRepository getNewPathRepository(final boolean useDefault) {
    final PathRepository pathRepository = new PathRepository();
    if (useDefault) {
      pathRepository
          .addPathDetector(new PathsExtractor(System.getProperty("pathutils.extrapaths")));
      pathRepository.addPathDetector(new PathsExtractor(System.getProperty("java.class.path")));
      pathRepository.addPathDetector(new ResourcesDetector("META-INF"));
      pathRepository.addPathDetector(new ResourcesDetector("autodetect"));
      pathRepository.addPathDetector(new ManifestClassPathDetector());
    }
    return pathRepository;
  }

  /**
   * Clear the singleton instance of the default path repository.
   */
  public static void reset() {
    synchronized (PathRepositoryFactory.class) {
      PathRepositoryFactory.defaultPathRepository = null;
    }
  }

  /**
   * Private constructor to forbid an instantiation of the class.
   */
  private PathRepositoryFactory() {
  }
}
