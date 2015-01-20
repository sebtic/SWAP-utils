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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Class TemporaryContentOutputStream.
 * 
 * @author Sébastien Aupetit
 */
class TemporaryContentOutputStream extends OutputStream {
  /** Indicate if the stream has already be closed */
  private final AtomicBoolean closed = new AtomicBoolean(false);

  /** The output stream to the file. */
  private final AtomicReference<BufferedOutputStream> bos = new AtomicReference<>(null);

  /** The in-memory output stream. */
  private final AtomicReference<ByteArrayOutputStream> baos = new AtomicReference<>();

  /** The byte counter. */
  private int count = 0;

  private final TemporaryContentHolder contentHolder;

  /**
   * Instantiates a new temporary output stream.
   */
  public TemporaryContentOutputStream(final TemporaryContentHolder contentHolder) {
    this.contentHolder = contentHolder;
    baos.set(new ByteArrayOutputStream(contentHolder.inMemoryMaxSize));
    contentHolder.allocateReference();
  }

  /*
   * (non-Javadoc)
   * @see java.io.OutputStream#close()
   */
  @Override
  public void close() {
    if (!closed.getAndSet(true)) {
      final ByteArrayOutputStream baosTemp = baos.get();
      if (baosTemp != null) {
        contentHolder.inMemory.set(baosTemp.toByteArray());
        baos.set(null); // free memory
      }
      @SuppressWarnings("resource")
      final BufferedOutputStream bosTemp = bos.get();
      if (bosTemp != null) {
        try {
          bosTemp.close();
        } catch (final IOException ignored) {
          // ignore errors
        }
        bos.set(null); // free memory
      }
      contentHolder.releaseReference();
    }
  }

  /*
   * (non-Javadoc)
   * @see java.io.OutputStream#flush()
   */
  @Override
  public void flush() throws IOException {
    final ByteArrayOutputStream baosTemp = baos.get();
    if (baosTemp != null) {
      baosTemp.flush();
    }
    final BufferedOutputStream bosTemp = bos.get();
    if (bosTemp != null) {
      bosTemp.flush();
    }
  }

  public boolean isClosed() {
    return closed.get();
  }

  /*
   * (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write(final int b) throws IOException {
    final ByteArrayOutputStream baosTemp = baos.get();
    if (baosTemp != null && count < contentHolder.inMemoryMaxSize) {
      baosTemp.write(b);
    } else {
      if (bos == null) {
        bos.set(new BufferedOutputStream(new FileOutputStream(contentHolder.inFile)));
        baosTemp.writeTo(bos.get());
        baos.set(null);
      }
      bos.get().write(b);
    }
    count++;
  }

}
