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
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * The Class ContentHolder.
 * 
 * @author Sébastien Aupetit
 */
public abstract class ContentHolder {

  public String getContentAsString(final String encoding, final int maxSize) {
    final StringBuilder sb = new StringBuilder();
    final char[] buf = new char[maxSize];
    try (Reader reader = new InputStreamReader(getInputStream(), encoding)) {
      int len;
      if ((len = reader.read(buf)) != -1) {
        sb.append(buf, 0, len);
      }
      return sb.toString();
    } catch (final IOException e) {
      return "";
    }

  }

  /**
   * Gets the content as string builder.
   * 
   * @param encoding the encoding
   * @return the content as string builder
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public StringBuilder getContentAsStringBuilder(final String encoding) throws IOException {
    final StringBuilder sb = new StringBuilder();
    final char[] buf = new char[4 * 1024];
    try (Reader reader = new InputStreamReader(getInputStream(), encoding)) {
      int len;
      while ((len = reader.read(buf)) != -1) {
        sb.append(buf, 0, len);
      }
    }
    return sb;
  }

  /**
   * Gets an input stream for the content.
   * 
   * @return the input stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract InputStream getInputStream() throws IOException;

  /**
   * Release the content holder. When all derived streams are closed and this
   * method is called then the content can be discarded if appropriate.
   */
  public abstract void release();

}
