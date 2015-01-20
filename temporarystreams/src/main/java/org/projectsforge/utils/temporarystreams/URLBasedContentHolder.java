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
package org.projectsforge.utils.temporarystreams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The Class URLBasedContentHolder.
 * 
 * @author Sébastien Aupetit
 */
public class URLBasedContentHolder extends ContentHolder {

  /** The url. */
  private final URL url;

  /**
   * Instantiates a new uRL based content holder.
   * 
   * @param url the url
   */
  public URLBasedContentHolder(final URL url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.projectsforge.utils.temporarystreams.ContentHolder#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    return url.openStream();
  }

  /*
   * (non-Javadoc)
   * @see org.projectsforge.utils.temporarystreams.ContentHolder#release()
   */
  @Override
  public void release() {
    // final nothing to do
  }
}
