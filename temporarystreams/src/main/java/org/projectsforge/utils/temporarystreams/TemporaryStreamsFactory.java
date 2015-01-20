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

import java.io.File;
import java.util.BitSet;
import java.util.UUID;

/**
 * A factory for creating TemporaryStreams objects.
 * 
 * @author Sébastien Aupetit
 */
public class TemporaryStreamsFactory {

  /** The in use id. */
  private final BitSet inUseId = new BitSet(128);

  /** The initialized. */
  private boolean initialized = false;

  /** The in memory max size. */
  private int inMemoryMaxSize;

  /** The prefix. */
  private String prefix;

  /** The suffix. */
  private String suffix;

  /** The directory. */
  private File directory;

  /** The Constant inMemoryMaxSizeProperty. */
  public static final String inMemoryMaxSizeProperty = "org.projectsforge.utils.temporarystreams.inmemorymaxsize";

  /** The Constant prefixProperty. */
  public static final String prefixProperty = "org.projectsforge.utils.temporarystreams.prefix";

  /** The Constant suffixProperty. */
  public static final String suffixProperty = "org.projectsforge.utils.temporarystreams.suffix";

  /** The Constant directoryProperty. */
  public static final String directoryProperty = "org.projectsforge.utils.temporarystreams.directory";

  /**
   * Allocate stream id.
   * 
   * @return the int
   */
  public int allocateStreamId() {
    synchronized (inUseId) {
      init();

      int next = inUseId.nextClearBit(0);
      if (next == -1) {
        next = inUseId.size();
        inUseId.set(next);
      }
      return next;
    }
  }

  /**
   * Gets the directory.
   * 
   * @return the directory
   */
  public File getDirectory() {
    init();
    return directory;
  }

  /**
   * Gets the in memory max size.
   * 
   * @return the in memory max size
   */
  public int getInMemoryMaxSize() {
    init();
    return inMemoryMaxSize;
  }

  /**
   * Gets the prefix.
   * 
   * @return the prefix
   */
  public String getPrefix() {
    init();
    return prefix;
  }

  /**
   * Gets the suffix.
   * 
   * @return the suffix
   */
  public String getSuffix() {
    init();
    return suffix;
  }

  /**
   * Inits the.
   */
  private void init() {
    synchronized (inUseId) {
      if (!initialized) {
        inMemoryMaxSize = Integer.parseInt(System.getProperty(inMemoryMaxSizeProperty, "65536"));
        prefix = System.getProperty(prefixProperty, "temp");
        suffix = System.getProperty(suffixProperty, ".tmp");
        directory = new File(new File(System.getProperty("java.io.tmpdir")), System.getProperty(
            directoryProperty, "org.projectsforge.utils.temporarystreams")
            + "-"
            + UUID.randomUUID().toString());
        if (!directory.exists()) {
          directory.mkdirs();
        }
        initialized = true;
      }
    }
  }

  /**
   * Release stream id.
   * 
   * @param id the id
   */
  public void releaseStreamId(final int id) {
    synchronized (inUseId) {
      inUseId.clear(id);
    }
  }

}
