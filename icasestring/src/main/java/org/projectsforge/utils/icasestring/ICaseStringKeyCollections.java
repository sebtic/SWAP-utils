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
package org.projectsforge.utils.icasestring;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CaseInsensitiveKeyCollections to add checking for collections and
 * maps of {@link ICaseString}.
 */
public final class ICaseStringKeyCollections {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ICaseStringKeyCollections.class);

  /**
   * Add checking on case insensitive map.
   * 
   * @param <T>
   *          the generic type
   * @param map
   *          the map
   * @return the checked map
   */
  public static <T> Map<ICaseString, T> caseInsensitiveMap(
      final Map<ICaseString, T> map) {
    return new Map<ICaseString, T>() {

      @Override
      public void clear() {
        map.clear();
      }

      @Override
      public boolean containsKey(final Object key) {
        if (key instanceof ICaseString) {
          return map.containsKey(key);
        } else if (key instanceof String) {
          ICaseStringKeyCollections.logger.warn(
              "containsKey called with a String ({}) instead of a CaseInsensitiveString on {}",
              key, map);
          ICaseStringKeyCollections.logger.warn("Current stack trace is:", new Exception());
          return map.containsKey(new ICaseString((String) key));
        } else {
          ICaseStringKeyCollections.logger
              .error(
                  "containsKey called with {} instead of a CaseInsensitiveString on {}. It's certainly an error",
                  key, map);
          ICaseStringKeyCollections.logger.error("Current stack trace is:", new Exception());
          return map.containsKey(key);
        }
      }

      @Override
      public boolean containsValue(final Object value) {
        return map.containsValue(value);
      }

      @Override
      public Set<java.util.Map.Entry<ICaseString, T>> entrySet() {
        return map.entrySet();
      }

      @Override
      public boolean equals(final Object obj) {
        return map.equals(obj);
      }

      @Override
      public T get(final Object key) {
        if (key instanceof ICaseString) {
          return map.get(key);
        } else if (key instanceof String) {
          ICaseStringKeyCollections.logger.warn(
              "get called with a String ({}) instead of a CaseInsensitiveString on {}", key, map);
          ICaseStringKeyCollections.logger.warn("Current stack trace is:", new Exception());
          return map.get(new ICaseString((String) key));
        } else {
          ICaseStringKeyCollections.logger
              .error(
                  "get called with {} instead of a CaseInsensitiveString on {}. It's certainly an error",
                  key, map);
          ICaseStringKeyCollections.logger.error("Current stack trace is:", new Exception());
          return map.get(key);
        }
      }

      @Override
      public int hashCode() {
        return map.hashCode();
      }

      @Override
      public boolean isEmpty() {
        return map.isEmpty();
      }

      @Override
      public Set<ICaseString> keySet() {
        return ICaseStringKeyCollections.caseInsensitiveSet(map.keySet());
      }

      @Override
      public T put(final ICaseString key, final T value) {
        return map.put(key, value);
      }

      @Override
      public void putAll(final Map<? extends ICaseString, ? extends T> m) {
        map.putAll(m);
      }

      @Override
      public T remove(final Object key) {
        if (key instanceof ICaseString) {
          return map.remove(key);
        } else if (key instanceof String) {
          ICaseStringKeyCollections.logger
              .warn("remove called with a String ({}) instead of a CaseInsensitiveString on {}",
                  key, map);
          ICaseStringKeyCollections.logger.warn("Current stack trace is:", new Exception());
          return map.remove(new ICaseString((String) key));
        } else {
          ICaseStringKeyCollections.logger
              .error(
                  "remove called with {} instead of a CaseInsensitiveString on {}. It's certainly an error",
                  key, map);
          ICaseStringKeyCollections.logger.error("Current stack trace is:", new Exception());
          return map.remove(key);
        }
      }

      @Override
      public int size() {
        return map.size();
      }

      @Override
      public String toString() {
        return map.toString();
      }

      @Override
      public Collection<T> values() {
        return map.values();
      }
    };

  }

  /**
   * Add checking on a case insensitive set.
   * 
   * @param set
   *          the set
   * @return the checked set
   */
  public static Set<ICaseString> caseInsensitiveSet(final Set<ICaseString> set) {
    return new Set<ICaseString>() {

      @Override
      public boolean add(final ICaseString e) {
        return set.add(e);
      }

      @Override
      public boolean addAll(final Collection<? extends ICaseString> c) {
        return set.addAll(c);
      }

      @Override
      public void clear() {
        set.clear();
      }

      @Override
      public boolean contains(final Object key) {
        if (key instanceof ICaseString) {
          return set.contains(key);
        } else if (key instanceof String) {
          ICaseStringKeyCollections.logger.warn(
              "contains called with a String ({}) instead of a CaseInsensitiveString on {}", key,
              set);
          ICaseStringKeyCollections.logger.warn("Current stack trace is:", new Exception());
          return set.contains(new ICaseString((String) key));
        } else {
          ICaseStringKeyCollections.logger
              .error(
                  "contains called with {} instead of a CaseInsensitiveString on {}. It's certainly an error",
                  key, set);
          ICaseStringKeyCollections.logger.error("Current stack trace is:", new Exception());
          return set.contains(key);
        }
      }

      @Override
      public boolean containsAll(final Collection<?> c) {
        return set.containsAll(c);
      }

      @Override
      public boolean equals(final Object obj) {
        return set.equals(obj);
      }

      @Override
      public int hashCode() {
        return set.hashCode();
      }

      @Override
      public boolean isEmpty() {
        return set.isEmpty();
      }

      @Override
      public Iterator<ICaseString> iterator() {
        return set.iterator();
      }

      @Override
      public boolean remove(final Object key) {
        if (key instanceof ICaseString) {
          return set.remove(key);
        } else if (key instanceof String) {
          ICaseStringKeyCollections.logger
              .warn("remove called with a String ({}) instead of a CaseInsensitiveString on {}",
                  key, set);
          ICaseStringKeyCollections.logger.warn("Current stack trace is:", new Exception());
          return set.remove(new ICaseString((String) key));
        } else {
          ICaseStringKeyCollections.logger
              .error(
                  "remove called with {} instead of a CaseInsensitiveString on {}. It's certainly an error",
                  key, set);
          ICaseStringKeyCollections.logger.error("Current stack trace is:", new Exception());
          return set.remove(key);
        }
      }

      @Override
      public boolean removeAll(final Collection<?> c) {
        return set.removeAll(c);
      }

      @Override
      public boolean retainAll(final Collection<?> c) {
        return set.retainAll(c);
      }

      @Override
      public int size() {
        return set.size();
      }

      @Override
      public Object[] toArray() {
        return set.toArray();
      }

      @Override
      public <T> T[] toArray(final T[] a) {
        return set.toArray(a);
      }

      @Override
      public String toString() {
        return set.toString();
      }
    };
  }

  /**
   * The Constructor.
   */
  private ICaseStringKeyCollections() {

  }
}
