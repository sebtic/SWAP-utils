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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Class TemporaryContentInputStream.
 * 
 * @author Sébastien Aupetit
 */
class TemporaryContentInputStream extends FilterInputStream {

  private final AtomicBoolean closed = new AtomicBoolean(false);

  private final TemporaryContentHolder contentHolder;

  /**
   * Instantiates a new temporary content input stream.
   * 
   * @param in the in
   */
  public TemporaryContentInputStream(final TemporaryContentHolder contentHolder, final InputStream in) {
    super(in);
    this.contentHolder = contentHolder;
    contentHolder.allocateReference();
  }

  @Override
  public void close() throws IOException {
    if (!closed.getAndSet(true)) {
      try {
        super.close();
      } finally {
        contentHolder.releaseReference();
      }
    }
  }
}
