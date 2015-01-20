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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extract and register the paths defined in the provided list of paths. The
 * paths are separated by the pathSeparator char.
 * 
 * @author Sébastien Aupetit
 */
public class PathsExtractor implements PathDetector {

  /** The list of paths. */
  private final String listOfPaths;

  private final Logger logger = LoggerFactory.getLogger(PathsExtractor.class);

  /**
   * The constructor.
   * 
   * @param listOfPaths the string containing the list of paths
   */
  public PathsExtractor(final String listOfPaths) {
    this.listOfPaths = listOfPaths;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.projectsforge.pathutils.manager.PathDetector#detect(org.projectsforge
   * .pathutils.manager.PathRepository)
   */
  @Override
  public void detect(final PathRepository classpathManager) {
    if (listOfPaths != null) {
      final StringTokenizer tokenizer = new StringTokenizer(listOfPaths, File.pathSeparator);
      while (tokenizer.hasMoreTokens()) {
        String path = tokenizer.nextToken();
        if ((path != null) && !path.equals("")) {
          path = new File(path).getAbsolutePath();
          try {
            classpathManager.addPath(new URL("file", "", path));
          } catch (final MalformedURLException e) {
            logger.warn("Can not convert {} to an URL", path);
          }
        }
      }
    }
  }

  /**
   * Gets the list of paths.
   * 
   * @return the list of paths
   */
  public String getListOfPaths() {
    return this.listOfPaths;
  }

  @Override
  public String toString() {
    return "PathsExtractor[" + listOfPaths + "]";
  }

}
