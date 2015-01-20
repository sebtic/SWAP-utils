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
package org.projectsforge.utils.visitor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A comparator used to order the calls to the methods from the most specific to
 * the most generic.
 * 
 * @author Sébastien Aupetit
 */
class MethodComparator implements Comparator<MethodDescriptor> {

  /** The Constant UNDEFINED. */
  private static final int UNDEFINED = 100;

  /** The index. */
  private final Map<String, Integer> index = new HashMap<>();

  /** The order. */
  private final int[][] order;

  /** The descriptor. */
  private final VisitorDelegatorDescriptor descriptor;

  /**
   * Instantiates a new method comparator.
   * 
   * @param descriptor the descriptor
   * @param visitorClass the visitor class
   * @param statistics the statistics
   */
  public MethodComparator(final VisitorDelegatorDescriptor descriptor, final String visitorClass,
      final List<MethodDescriptor> statistics) {
    this.descriptor = descriptor;

    final int size = statistics.size();

    for (int i = 0; i < size; ++i) {
      index.put(statistics.get(i).className, i);
    }
    order = new int[size][size];
    for (int i = 0; i < size; ++i) {
      Arrays.fill(order[i], UNDEFINED);
    }

    // build order
    for (int i = 0; i < size; ++i) {
      for (int j = 0; j < size; ++j) {
        order[i][j] = internalCompare(statistics.get(i), statistics.get(j));
      }
    }

    // check for inconsistencies
    boolean valid = true;
    for (int i = 0; i < size; ++i) {
      // Reflexivity
      if (order[i][i] != 0) {
        valid = false;
      }
      for (int j = 0; j < i; ++j) {
        // Antisymmetry
        if (order[i][j] != -order[j][i]) {
          valid = false;
        }
        for (int k = 0; k < size; ++k) {
          // Transitivity
          if (order[i][j] == order[j][k]) {
            if (order[i][j] != order[i][k]) {
              valid = false;
            }
          }
          if (order[j][i] == order[i][k]) {
            if (order[j][i] != order[j][k]) {
              valid = false;
            }
          }
        }
      }
    }

    if (!valid) {
      if (descriptor.logger.isErrorEnabled()) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; ++i) {
          sb.append(i + 1).append(" => ").append(statistics.get(i).className).append("\n");
        }
        sb.append("\n");
        for (int i = 0; i < size; ++i) {
          sb.append(String.format("%1$-5s ", i + 1));
          for (int j = 0; j < size; ++j) {
            switch (order[i][j]) {
              case -1:
                sb.append(" < ");
                break;
              case 1:
                sb.append(" > ");
                break;
              case 0:
                sb.append(" = ");
                break;
              case UNDEFINED:
                sb.append(" ? ");
                break;
              default:
                sb.append(order[i][j]);
            }
          }
          sb.append("\n");
        }

        descriptor.logger.error(
            "Incoherent order detected for {}. Action must be performed to proceed further.\n{}",
            visitorClass, sb);
      }
      throw new IllegalStateException(
          "Incoherent order detected. Action mus be performed to proceed further with "
              + visitorClass);
    }
  }

  /*
   * (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(final MethodDescriptor first, final MethodDescriptor second) {
    return order[index.get(first.className)][index.get(second.className)];
  }

  /**
   * Internal compare.
   * 
   * @param first the first
   * @param second the second
   * @return the int
   */
  private int internalCompare(final MethodDescriptor first, final MethodDescriptor second) {

    // identity ?
    if (first.parameterClass.equals(second.parameterClass)) {

      return 0;
    }

    // superclass relationships
    if (first.parameterClass.isAssignableFrom(second.parameterClass)) {
      // first is a superclass of second => put first after second
      return 1;
    }

    // superclass relationships
    if (second.parameterClass.isAssignableFrom(first.parameterClass)) {
      // second is a superclass of first => put first before second
      return -1;
    }

    if ((first.parameterClass.isInterface() && !second.parameterClass.isInterface())
        || (!first.parameterClass.isInterface() && second.parameterClass.isInterface())
        || (!first.parameterClass.isInterface() && !second.parameterClass.isInterface())) {
      // two unrelated class/interfaces with no risk of ambiguities
      if (first.counter.get() > second.counter.get()) {
        return -1;
      } else if (first.counter.get() < second.counter.get()) {
        return 1;
      } else {
        final int res = first.className.compareTo(second.className);
        if (res < 0) {
          return -1;
        } else if (res > 0) {
          return 1;
        } else {
          return 0;
        }
      }
    }

    if (first.parameterClass.isInterface() && second.parameterClass.isInterface()) {
      // two unrelated interfaces => ambiguities are possible if something
      // implements the two interfaces
      // => require annotations and check for contradictory choices
      final VisitingPriority firstVp = first.method.getAnnotation(VisitingPriority.class);
      final VisitingPriority secondVp = second.method.getAnnotation(VisitingPriority.class);

      if (firstVp != null && secondVp != null) {

        final List<Class<?>> firstIsBefore = Arrays.asList(firstVp.before());
        final List<Class<?>> secondIsBefore = Arrays.asList(secondVp.before());

        if (firstIsBefore.contains(second.parameterClass)
            && !secondIsBefore.contains(first.parameterClass)) {
          return -1;
        }
        if (secondIsBefore.contains(first.parameterClass)
            && !firstIsBefore.contains(second.parameterClass)) {
          return -1;
        }
      }

      descriptor.logger
          .error(
              "Ambiguities between {} and {} are not resolved. Can not proceed further. Use @VisitingPriority to proceed further.",
              first.className, second.className);
      throw new IllegalStateException("Ambiguities between " + first.className + " and "
          + second.className
          + " are not resolved. Can not proceed further. Use @VisitingPriority to proceed further.");

    } else {
      throw new IllegalStateException("Programmatic error");
    }
  }
}
