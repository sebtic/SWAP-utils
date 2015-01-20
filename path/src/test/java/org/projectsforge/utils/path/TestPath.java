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
import java.util.HashSet;
import java.util.Properties;
import junit.framework.Assert;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

/**
 * The Class TestPath.
 * 
 * @author Sébastien Aupetit
 */
public class TestPath {

  /**
   * Test.
   */
  @Test
  public void test() {
    final Properties initialLog4jSettings = new Properties();
    initialLog4jSettings.setProperty("log4j.rootCategory", "DEBUG, CONSOLE");
    initialLog4jSettings.setProperty("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
    initialLog4jSettings.setProperty("log4j.appender.CONSOLE.Threshold", "DEBUG");
    initialLog4jSettings.setProperty("log4j.appender.CONSOLE.layout",
        "org.apache.log4j.PatternLayout");
    initialLog4jSettings.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern",
        "%-4r [%t] %-5p %c %x - %m%n");
    initialLog4jSettings.setProperty("log4j.logger.org.projectsforge.utils.path", "DEBUG");
    PropertyConfigurator.configure(initialLog4jSettings);

    final PathRepository pr = PathRepositoryFactory.getDefaultPathRepository();
    final URISearcher searcher = new URISearcher();
    searcher.setSearchPaths(pr.getPaths());
    final JRegExPathMatcher matcher1 = new JRegExPathMatcher(new jregex.Pattern(".*test\\.mark$"),
        new HashSet<URL>());
    final JRegExPathMatcher matcher2 = new JRegExPathMatcher(new jregex.Pattern(".*\\.mark$"),
        new HashSet<URL>());
    searcher.search(new PathMatcherCollection(matcher1, matcher2));

    Assert.assertEquals(1, matcher1.getMatchedPaths().size());
    Assert.assertEquals(1, matcher2.getMatchedPaths().size());
  }

}
