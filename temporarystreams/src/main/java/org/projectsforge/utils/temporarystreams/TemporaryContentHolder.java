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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The class holding a temporary content. Reference counting is used to detect
 * when the content is no more necessary.
 * 
 * @author Sébastien Aupetit
 */
public class TemporaryContentHolder extends ContentHolder {

  /** The id. */
  private final int id;

  /** The in memory. */
  final AtomicReference<byte[]> inMemory = new AtomicReference<>();

  /** The in file. */
  final File inFile;

  /** The temporary stream factory. */
  private final TemporaryStreamsFactory temporaryStreamFactory;

  /** The reference counter. */
  private final AtomicInteger referenceCounter = new AtomicInteger(0);

  /** The in memory max size. */
  final int inMemoryMaxSize;

  private final AtomicReference<TemporaryContentOutputStream> outputStream = new AtomicReference<>(null);

  /**
   * Instantiates a new temporary streams holder.
   * 
   * @param temporaryStreamsFactory the temporary streams factory
   */
  public TemporaryContentHolder(final TemporaryStreamsFactory temporaryStreamsFactory) {
    this.temporaryStreamFactory = temporaryStreamsFactory;
    this.id = temporaryStreamsFactory.allocateStreamId();
    this.inMemoryMaxSize = temporaryStreamsFactory.getInMemoryMaxSize();
    this.inFile = new File(temporaryStreamsFactory.getDirectory(), temporaryStreamsFactory.getPrefix() + id
        + temporaryStreamsFactory.getSuffix());
    this.inFile.deleteOnExit();
    allocateReference();
    outputStream.set(new TemporaryContentOutputStream(this));
  }

  /**
   * Allocate reference.
   */
  void allocateReference() {
    referenceCounter.incrementAndGet();
  }

  /*
   * (non-Javadoc)
   * @see
   * org.projectsforge.utils.temporarystreams.ContentHolder#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws FileNotFoundException {
    try (final TemporaryContentOutputStream out = outputStream.get()) {
      if (out == null) {
        throw new IllegalStateException("OutputStream has not been acquired");
      }

      // close outputstrem if not already done
      out.close();
    }

    final byte[] data = inMemory.get();
    if (inMemory.get() == null) {
      return new TemporaryContentInputStream(this, new BufferedInputStream(new FileInputStream(inFile)));
    } else {
      return new TemporaryContentInputStream(this, new ByteArrayInputStream(data));
    }
  }

  /**
   * Gets the output stream.
   * 
   * @return the output stream
   */
  public OutputStream getOutputStream() {
    return outputStream.get();
  }

  @Override
  public void release() {
    releaseReference();
  }

  /**
   * Release reference.
   */
  void releaseReference() {
    referenceCounter.decrementAndGet();
    if (referenceCounter.get() == 0) {
      // delete file
      inFile.delete();
      // free unnecessary buffer
      inMemory.set(null);
      // free output stream
      outputStream.set(null);
      temporaryStreamFactory.releaseStreamId(id);
    } else if (referenceCounter.get() < 0) {
      throw new IllegalStateException("TemporaryContentHolder too many releaseReference calls");
    }
  }

  @Override
  public String toString() {
    String content;
    final TemporaryContentOutputStream out = outputStream.get();
    if (out != null && out.isClosed()) {
      content = getContentAsString("US-ASCII", 2048);
    } else {
      content = "";
    }
    return "[id=" + id + ", refcount=" + referenceCounter + ", inFile=" + inFile + ", content=" + content + "]";
  }
}
