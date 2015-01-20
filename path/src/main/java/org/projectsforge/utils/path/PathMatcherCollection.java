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
import java.util.List;

/**
 * A {@link PathMatcher} which allow to match paths against a collection of.
 * {@link PathMatcher}.
 * 
 * @author Sébastien Aupetit
 */
public class PathMatcherCollection implements PathMatcher {

  /** The path matchers. */
  private final List<PathMatcher> pathMatchers = new ArrayList<PathMatcher>();

  /**
   * Instantiates a new path matcher collection.
   * 
   * @param pathMatchers the path matchers
   */
  public PathMatcherCollection(final PathMatcher... pathMatchers) {
    for (final PathMatcher matcher : pathMatchers) {
      addPathMatcher(matcher);
    }
  }

  /**
   * Adds a path matcher.
   * 
   * @param pathMatcher the path matcher
   */
  public void addPathMatcher(final PathMatcher pathMatcher) {
    pathMatchers.add(pathMatcher);
  }

  /*
   * (non-Javadoc)
   * @see org.projectsforge.pathutils.PathMatcher#match(java.net.URI)
   */
  @Override
  public void match(final URL candidate) {
    for (final PathMatcher matcher : pathMatchers) {
      matcher.match(candidate);
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return pathMatchers.toString();
  }

}
