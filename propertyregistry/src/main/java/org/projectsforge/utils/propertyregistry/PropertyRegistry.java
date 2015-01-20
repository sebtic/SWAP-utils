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
package org.projectsforge.utils.propertyregistry;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.projectsforge.utils.annotations.AnnotationScanner;
import org.projectsforge.utils.annotations.Detected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PropertyRegistry.
 * 
 * @author Sébastien Aupetit
 */
public class PropertyRegistry {

  /** The properties. */
  private final Map<String, Property<?>> properties = new TreeMap<>();

  /** The property holders. */
  private final Set<Class<? extends PropertyHolder>> propertyHolders = new TreeSet<>(
      new Comparator<Class<? extends PropertyHolder>>() {
        @Override
        public int compare(final Class<? extends PropertyHolder> o1, final Class<? extends PropertyHolder> o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(PropertyRegistry.class);

  /**
   * Instantiates a new property registry.
   * 
   * @param annotationScanner the annotation scanner
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  public PropertyRegistry(final AnnotationScanner annotationScanner) throws InstantiationException,
      IllegalAccessException {
    for (final Class<?> clazz : annotationScanner.getClasses(AnnotationScanner.INCLUDE_PLAIN_CLASSES, true,
        new Class<?>[] { Detected.class }).values()) {
      if (PropertyHolder.class.isAssignableFrom(clazz)) {
        registerPropertyHolder(clazz.asSubclass(PropertyHolder.class));
      }
    }
  }

  /**
   * Gets the properties.
   * 
   * @return the properties
   */
  public Map<String, Property<?>> getProperties() {
    return properties;
  }

  /**
   * Gets the properties names.
   * 
   * @return the properties names
   */
  public Set<String> getPropertiesNames() {
    return properties.keySet();
  }

  /**
   * Gets the property holders.
   * 
   * @return the property holders
   */
  public Set<Class<? extends PropertyHolder>> getPropertyHolders() {
    return propertyHolders;
  }

  /**
   * Gets the property value.
   * 
   * @param key the key
   * @return the property value
   */
  public String getPropertyValue(final String key) {
    final Property<?> property = properties.get(key);
    if (property != null) {
      return property.getHeldValue();
    } else {
      logger.warn("Undefined property retrieved: {}", key);
      return null;
    }
  }

  /**
   * Load.
   * 
   * @param file the file
   */
  public void load(final File file) {
    try (final InputStream is = new FileInputStream(file)) {
      final Properties properties = new Properties();
      properties.load(is);

      for (final Entry<Object, Object> entry : properties.entrySet()) {
        setProperty((String) entry.getKey(), (String) entry.getValue());
      }
    } catch (final IOException e) {
      logger.warn("Can not load properties from {}. Ignoring file.", file);
    }
  }

  /**
   * Override properties.
   * 
   * @param overridedProperties the overrided properties
   */
  public void overrideProperties(final Properties overridedProperties) {
    for (final Entry<Object, Object> entry : overridedProperties.entrySet()) {
      overrideProperty((String) entry.getKey(), (String) entry.getValue());
    }
  }

  /**
   * Override property.
   * 
   * @param key the key
   * @param overridedValue the overrided value
   */
  public void overrideProperty(final String key, final String overridedValue) {
    final Property<?> property = properties.get(key);
    if (property != null) {
      property.setValueHolder(new OverridedValueHolder(property.getValueHolder(), overridedValue));
    } else {
      logger.warn("Can not override non existing property {}", key);
    }
  }

  /**
   * Register property holder.
   * 
   * @param propertyHolderClass the property holder class
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  private void registerPropertyHolder(final Class<? extends PropertyHolder> propertyHolderClass)
      throws InstantiationException, IllegalAccessException {

    boolean atLeastOneProperty = false;

    for (final Field field : propertyHolderClass.getFields()) {
      if ((field.getModifiers() & Modifier.FINAL) == 0 || (field.getModifiers() & Modifier.PUBLIC) == 0
          || (field.getModifiers() & Modifier.STATIC) == 0) {
        logger.warn("Property {} in {} must be public, static and final.", field.getName(),
            propertyHolderClass.getName());
        throw new IllegalArgumentException("Property " + field.getName() + " in " + propertyHolderClass.getName()
            + " must be public, static and final.");
      }

      final Object value = field.get(null);
      if (value instanceof Property) {
        final Property<?> property = (Property<?>) value;
        if (properties.containsKey(property.getName())) {
          throw new IllegalArgumentException("Property " + property.getName() + " defined in "
              + propertyHolderClass.getName() + " has already been registered");
        }

        atLeastOneProperty = true;
        properties.put(property.getName(), property);
        property.setPropertyregistry(this);
        logger.debug("Detected property in {}: {}", propertyHolderClass.getName(), property.getName());
      }

    }

    if (atLeastOneProperty) {
      propertyHolders.add(propertyHolderClass);
      logger.info("Detected property holder class {}", propertyHolderClass.getName());
    }
  }

  /**
   * Save.
   * 
   * @param file the file
   */
  public void save(final File file) {
    final Properties data = new Properties();
    for (final Property<?> property : properties.values()) {
      property.save(data);
    }

    try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
      data.store(os, "# saved on " + new Date());
    } catch (final IOException e) {
      logger.warn("Can not save properties to {}. Ignoring save request.", file);
    }
  }

  /**
   * Sets the property.
   * 
   * @param key the key
   * @param value the value
   */
  public void setProperty(final String key, final String value) {
    final Property<?> property = properties.get(key);
    if (property != null) {
      property.setHeldValue(value);
    } else {
      logger.warn("Can not set non existing property {}", key);
    }
  }
}
