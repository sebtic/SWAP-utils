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
package org.projectsforge.utils.events;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A fast and simplified event listener list with lock-free retrieving of the
 * listeners.
 * 
 * @param <TEventListener> the type of the event listeners
 * @author Sébastien Aupetit
 */
public class EventListenerList<TEventListener> implements Serializable {

  /** The serialVersionUID. */
  private static final long serialVersionUID = 0;

  /** The list of the listeners. */
  protected transient Object[] listenerList = new Object[0];

  /**
   * Add an event listener.
   * 
   * @param listener the event listener
   */
  public synchronized void add(final TEventListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("Can not manage null event listener");
    }
    if (listenerList.length == 0) {
      listenerList = new Object[] { listener };
    } else {
      final int len = listenerList.length;
      final Object[] tmp = new Object[len + 1];
      System.arraycopy(listenerList, 0, tmp, 0, len);
      tmp[len] = listener;
      listenerList = tmp;
    }
  }

  /**
   * Return a non null list of listeners.
   * 
   * @return the list of listeners
   */
  public Object[] getListenerList() {
    return listenerList;
  }

  /**
   * Deserialize the list.
   * 
   * @param s the ObjectInputStream
   * @throws IOException if an IO error occur
   * @throws ClassNotFoundException if the class of a serialized object can not
   *           be found
   */
  @SuppressWarnings("unchecked")
  private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException {
    listenerList = new Object[0];
    s.defaultReadObject();
    final int count = s.readInt();

    for (int i = 0; i < count; ++i) {
      add((TEventListener) s.readObject());
    }
  }

  /**
   * Remove an event listener from the list.
   * 
   * @param listener the listener
   */
  public synchronized void remove(final TEventListener listener) {

    final Object[] list = listenerList;

    int index = -1;
    for (int i = list.length; i >= 0; --i) {
      if (list[i].equals(listener)) {
        index = i;
        break;
      }
    }

    if (index != -1) {
      final Object[] tmp = new Object[list.length - 1];
      System.arraycopy(list, 0, tmp, 0, index);
      if (index < tmp.length) {
        System.arraycopy(list, index + 1, tmp, index, tmp.length - index);
      }
      listenerList = (tmp.length == 0) ? new Object[0] : tmp;
    }
  }

  /**
   * The toString implementation.
   * 
   * @return the string representation
   */
  @Override
  public String toString() {
    final Object[] list = listenerList;
    String s = "EventListenerList: ";
    s += list.length + " listeners: ";
    for (final Object element : list) {
      s += " " + element;
    }
    return s;
  }

  /**
   * Serialize the list.
   * 
   * @param s the ObjectOutputStream
   * @throws IOException if an IO error occurs while writing objects
   */
  private void writeObject(final ObjectOutputStream s) throws IOException {
    final Object[] list = listenerList;
    s.defaultWriteObject();

    final int count = list.length;
    s.writeInt(count);
    for (int i = 0; i < count; ++i) {
      s.writeObject(list[i]);
    }
  }

}
