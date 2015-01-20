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
import java.util.Collection;
import jregex.Matcher;
import jregex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link PathMatcher} which matches paths according to a jregex regex.
 * 
 * @author Sébastien Aupetit
 */
public class JRegExPathMatcher<URLCollection extends Collection<URL> > implements PathMatcher {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(JavaRegExPatchMatcher.class);

  /** The paths. */
  private final URLCollection paths;

  /** The pattern. */
  private final Pattern pattern;

  /** The matcher. */
  private final Matcher matcher;

  /**
   * Instantiates a new JRegEx path matcher.
   * 
   * @param pattern the matching pattern
   * @param paths the collection used to store the matched paths
   */
  public JRegExPathMatcher(final Pattern pattern, final URLCollection paths) {
    this.pattern = pattern;
    this.paths = paths;
    matcher = pattern.matcher("");
  }

  /**
   * Gets the matched paths.
   * 
   * @return the matched paths
   */
  public URLCollection getMatchedPaths() {
    return paths;
  }

  /*
   * (non-Javadoc)
   * @see org.projectsforge.pathutils.PathMatcher#match(java.net.URI)
   */
  @Override
  public void match(final URL candidate) {
    matcher.setTarget(candidate.toString());
    if (matcher.matches()) {
      JRegExPathMatcher.logger.debug("Matched path: {}", candidate);
      paths.add(candidate);
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "JRegExPathMatcher[" + pattern.toString() + "]";
  }
}
