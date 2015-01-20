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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * The detector which extracts paths from resources location.
 * 
 * @author Sébastien Aupetit
 */
public class ResourcesDetector implements PathDetector {

  /** The logger. */
  private final org.slf4j.Logger logger = org.slf4j.LoggerFactory
      .getLogger(ResourcesDetector.class);

  /** The resource name. */
  private final String resourceName;

  /**
   * Instantiates a new resources detector.
   * 
   * @param resourceName the resource name
   */
  public ResourcesDetector(final String resourceName) {
    this.resourceName = resourceName;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.projectsforge.pathutils.manager.PathDetector#detect(org.projectsforge
   * .pathutils.manager.PathRepository)
   */
  @Override
  public void detect(final PathRepository classpathManager) {
    try {
      final Enumeration<URL> urls = Thread.currentThread().getContextClassLoader()
          .getResources(resourceName);

      while (urls.hasMoreElements()) {
        final URL url = urls.nextElement();
        String path = url.toString();

        final int index = path.lastIndexOf(resourceName);
        if (index != -1) {

          if (path.startsWith("jar:")) {
            // it is a jar URL : remove protocol and in archive part
            path = path.substring(4);
            path = path.substring(0, path.indexOf("!/"));
          } else {
            // it is a file URL
            path = path.substring(0, index - 1);
          }
          classpathManager.addPath(new URL(path));
        }

      }
    } catch (final IOException e) {
      logger.warn("Unable to list resources " + resourceName, e);
    }
  }

  /**
   * Gets the resource name.
   * 
   * @return the resource name
   */
  public String getResourceName() {
    return this.resourceName;
  }

  @Override
  public String toString() {
    return "ResourcesDetector[" + resourceName + "]";
  }
}
