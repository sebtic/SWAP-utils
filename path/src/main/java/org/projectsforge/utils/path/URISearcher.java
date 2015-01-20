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
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class managing searches of files from a set of paths.
 * 
 * @author Sébastien Aupetit
 */
public class URISearcher {

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(URISearcher.class);

  /** The paths where the files are searched. */
  protected final Set<URL> searchPaths = new HashSet<URL>();

  /**
   * Instantiates a new URI searcher.
   */
  public URISearcher() {
  }

  /**
   * Search files with the given {@link PathMatcher}.
   * 
   * @param pathMatcher the path matcher
   */
  public void search(final PathMatcher pathMatcher) {
    synchronized (this) {
      logger.info("Searching files matching {}", pathMatcher);
      for (final URL path : searchPaths) {
        logger.debug("Searching in {}", path);

        if ("file".equals(path.getProtocol()) && path.getPath().toLowerCase().endsWith(".jar")) {
          try {
            searchJar(pathMatcher, new URL("jar", "", path.toString() + "!/"));
          } catch (final MalformedURLException e) {
            logger.warn("Can not search in " + path, e);
          }
        } else if ("jar".equals(path.getProtocol())) {
          searchJar(pathMatcher, path);
        } else {
          searchDirectory(pathMatcher, path);
        }
      }

    }
  }

  /**
   * Search in directory for files.
   * 
   * @param pathMatcher the path matcher
   * @param dirURI the directory URI
   */
  private void searchDirectory(final PathMatcher pathMatcher, final URL dirURL) {
    File[] sub;
    try {
      sub = new File(dirURL.toURI()).listFiles();
    } catch (final URISyntaxException e1) {
      logger.warn("{} can not be converted to URI", dirURL);
      return;
    }

    if (sub != null) {
      for (int i = sub.length - 1; i >= 0; --i) {
        URL url;
        try {
          url = sub[i].toURI().toURL();
        } catch (final MalformedURLException e) {
          logger.warn("Can not convert " + sub[i] + " to URL", e);
          continue;
        }
        if (sub[i].isDirectory()) {
          searchDirectory(pathMatcher, url);
        } else {
          pathMatcher.match(url);
        }
      }
    }
  }

  /**
   * Search in JAR file for files.
   * 
   * @param pathMatcher the path matcher
   * @param path the path
   */
  private void searchJar(final PathMatcher pathMatcher, final URL path) {
    try {
      final JarURLConnection connection = (JarURLConnection) path.openConnection();
      try (final JarFile file = connection.getJarFile()) {
        final Enumeration<JarEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
          final String name = entries.nextElement().getName();
          try {
            pathMatcher.match(new URL(path, name));
          } catch (final MalformedURLException e) {
            logger.warn("Can not create an URL from {} and {}", path, name);
          }
        }
      }
    } catch (final IOException e) {
      logger.warn("Can not read {}", path);
    }

  }

  /**
   * Sets the search paths.
   * 
   * @param searchPaths the new search paths
   */
  public void setSearchPaths(final Set<URL> searchPaths) {
    this.searchPaths.clear();
    this.searchPaths.addAll(searchPaths);
  }
}
