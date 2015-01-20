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
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.Manifest;

/**
 * Extract and register the paths defined by the Class-Path property of all
 * META-INF/MANIFEST.MF property files.
 * 
 * @author Sébastien Aupetit
 */
public class ManifestClassPathDetector implements PathDetector {

  /** The logger. */
  private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ManifestClassPathDetector.class);

  /*
   * (non-Javadoc)
   * @see
   * org.projectsforge.pathutils.manager.PathDetector#detect(org.projectsforge
   * .pathutils.manager.PathRepository)
   */
  @Override
  public void detect(final PathRepository classpathManager) {
    Enumeration<URL> urls;
    try {
      urls = Thread.currentThread().getContextClassLoader().getResources("META-INF/MANIFEST.MF");

      while (urls.hasMoreElements()) {
        final URL url = urls.nextElement();
        try (final InputStream inputStream = url.openStream()) {
          final Manifest manifest = new Manifest(inputStream);
          final String classpath = manifest.getMainAttributes().getValue("Class-Path");
          if (classpath != null) {
            logger.debug("Found Class-Path in MANIFEST.MF at " + url.toExternalForm());
            final StringTokenizer tokenizer = new StringTokenizer(classpath, " ");
            while (tokenizer.hasMoreTokens()) {
              final String token = tokenizer.nextToken();
              if (token.startsWith("file:") || token.startsWith("jar:")) {
                classpathManager.addPath(new URL(token));
              } else {
                logger.warn("Unsupported path URI found ({}) in {}", token, url.toExternalForm());
              }
            }
          }
        } catch (final IOException e) {
          logger.debug("Can not detect META-INF/MANIFEST.MF files", e);
        }
      }
    } catch (final IOException e) {
      logger.debug("Can not detect META-INF/MANIFEST.MF files", e);
    }
  }

  @Override
  public String toString() {
    return "ManifestClassPathDetector";
  }
}
