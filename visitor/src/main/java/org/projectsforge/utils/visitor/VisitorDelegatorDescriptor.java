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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import org.apache.commons.io.output.StringBuilderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class used to hold the information about the {@link VisitorDelegator}s.
 * 
 * @author Sébastien Aupetit
 */
public class VisitorDelegatorDescriptor {

  /**
   * Gets the class.
   * 
   * @param type the type
   * @return the class
   */
  private static Class<?> getClass(final Type type) {
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      return VisitorDelegatorDescriptor.getClass(((ParameterizedType) type).getRawType());
    } else if (type instanceof GenericArrayType) {
      final Type componentType = ((GenericArrayType) type).getGenericComponentType();
      final Class<?> componentClass = VisitorDelegatorDescriptor.getClass(componentType);
      if (componentClass != null) {
        return Array.newInstance(componentClass, 0).getClass();
      } else {
        throw new IllegalArgumentException("Could not determine the class of the type argument");
      }
    } else {
      throw new IllegalArgumentException("Could not determine the class of the type argument");
    }
  }

  /** The visitor class. */
  private final Class<?> visitorClass;

  /** The profiled delegator. */
  public final VisitorDelegator profiledDelegator;

  /** The unprofiled delegator. */
  public final VisitorDelegator unprofiledDelegator;

  /** The preVisit methods statistics. */
  private final List<MethodDescriptor> preVisitMethodsStatistics = new ArrayList<>();

  /** The visit methods statistics. */
  private final List<MethodDescriptor> visitMethodsStatistics = new ArrayList<>();

  /** The postVisit methods statistics. */
  private final List<MethodDescriptor> postVisitMethodsStatistics = new ArrayList<>();

  /** The input class. */
  private final Class<?> inputClass;

  /** The state class. */
  private final Class<?> stateClass;

  /** The output class. */
  private final Class<?> outputClass;

  /** The logger. */
  final Logger logger;

  /**
   * Instantiates a new descriptor.
   * 
   * @param visitorClass the visitor class
   */
  public VisitorDelegatorDescriptor(final Class<?> visitorClass) {

    logger = LoggerFactory.getLogger(VisitorDelegatorDescriptor.class.getName() + "." + visitorClass.getName());

    this.visitorClass = visitorClass;

    // determine generic parameters
    // 1. determine child class of Visitor
    Class<?> lastClass = visitorClass;
    while (lastClass.getSuperclass() != Visitor.class) {
      lastClass = lastClass.getSuperclass();
    }
    // 2. determine the parameterized superclass i.e. the Visitor class
    final ParameterizedType superClass = (ParameterizedType) lastClass.getGenericSuperclass();
    // 3. extract generic arguments
    final Type[] arguments = superClass.getActualTypeArguments();

    if (arguments == null || arguments.length == 0) {
      inputClass = Object.class;
      stateClass = Object.class;
      outputClass = Object.class;
    } else {
      inputClass = VisitorDelegatorDescriptor.getClass(arguments[0]);
      stateClass = VisitorDelegatorDescriptor.getClass(arguments[1]);
      outputClass = VisitorDelegatorDescriptor.getClass(arguments[2]);
    }

    // collect methods through reflection
    final Map<Class<?>, Method> preVisitMethods = new HashMap<>();
    collectMethods("preVisit", visitorClass, preVisitMethods);

    final Map<Class<?>, Method> visitMethods = new HashMap<>();
    collectMethods("visit", visitorClass, visitMethods);

    final Map<Class<?>, Method> postVisitMethods = new HashMap<>();
    collectMethods("postVisit", visitorClass, postVisitMethods);

    // fill statistics
    for (final Entry<Class<?>, Method> entry : preVisitMethods.entrySet()) {
      preVisitMethodsStatistics.add(new MethodDescriptor(entry.getKey(), entry.getValue()));
    }
    for (final Entry<Class<?>, Method> entry : visitMethods.entrySet()) {
      visitMethodsStatistics.add(new MethodDescriptor(entry.getKey(), entry.getValue()));
    }
    for (final Entry<Class<?>, Method> entry : postVisitMethods.entrySet()) {
      postVisitMethodsStatistics.add(new MethodDescriptor(entry.getKey(), entry.getValue()));
    }

    // load statistics
    loadStatistics();

    // sort methods
    Collections.sort(preVisitMethodsStatistics, new MethodComparator(this, visitorClass.getName(),
        preVisitMethodsStatistics));
    Collections
        .sort(visitMethodsStatistics, new MethodComparator(this, visitorClass.getName(), visitMethodsStatistics));
    Collections.sort(postVisitMethodsStatistics, new MethodComparator(this, visitorClass.getName(),
        postVisitMethodsStatistics));

    // Generate and instantiate the delegators
    try {
      unprofiledDelegator = (VisitorDelegator) generateClass(false).newInstance();
      unprofiledDelegator.setCounters(null, null, null);
      unprofiledDelegator.setOutputClass(outputClass);

      profiledDelegator = (VisitorDelegator) generateClass(true).newInstance();
      final AtomicInteger[] preVisitCounters = new AtomicInteger[preVisitMethodsStatistics.size()];
      for (int i = 0; i < preVisitMethodsStatistics.size(); ++i) {
        preVisitCounters[i] = preVisitMethodsStatistics.get(i).counter;
      }
      final AtomicInteger[] visitCounters = new AtomicInteger[visitMethodsStatistics.size()];
      for (int i = 0; i < visitMethodsStatistics.size(); ++i) {
        visitCounters[i] = visitMethodsStatistics.get(i).counter;
      }
      final AtomicInteger[] postVisitCounters = new AtomicInteger[postVisitMethodsStatistics.size()];
      for (int i = 0; i < postVisitMethodsStatistics.size(); ++i) {
        postVisitCounters[i] = postVisitMethodsStatistics.get(i).counter;
      }
      profiledDelegator.setCounters(preVisitCounters, visitCounters, postVisitCounters);
      profiledDelegator.setOutputClass(outputClass);
    } catch (CannotCompileException | NotFoundException | InstantiationException | IllegalAccessException e) {
      logger.error("Delegators can not be created for " + visitorClass.getName(), e);
      throw new IllegalStateException("Delegators can not be created for " + visitorClass.getName(), e);
    }
  }

  /**
   * Collects the methods.
   * 
   * @param name the base name of the methods (starts with)
   * @param clazz the clazz owning the methods
   * @param results the results
   */
  private void collectMethods(final String name, final Class<?> clazz, final Map<Class<?>, Method> results) {
    if (clazz == null || clazz == Object.class) {
      return;
    }

    collectMethods(name, clazz.getSuperclass(), results);

    for (final Method method : clazz.getDeclaredMethods()) {
      if (method.getName().startsWith(name)) {
        if (!Modifier.isPackage(method.getModifiers()) && !Modifier.isPublic(method.getModifiers())) {
          logger.error("Method {} in {} must have public or package visibility", method, clazz.getName());
          throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
              + " must have public or package visibility");
        }

        final Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length == 0) {
          logger.error("Method {} in {} has no parameter", method, clazz.getName());
          throw new IllegalArgumentException("Method " + method + " in " + clazz.getName() + " has no parameter");
        }
        if (parameters.length >= 1) {
          if (!inputClass.isAssignableFrom(parameters[0])) {
            logger.error("Method {} in {} has an invalid parameter", method, clazz.getName());
            throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
                + " has an invalid parameter");
          }
        }

        if (name.startsWith("preVisit")) {
          switch (parameters.length) {
            case 1:
              break;
            case 2:
              if (!stateClass.isAssignableFrom(parameters[1])) {
                logger.error("Method {} in {} has an invalid second parameter", method, clazz.getName());
                throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
                    + " has an invalid second parameter");
              }
              break;
            default:
              logger.error("Method {} in {} has too many parameters", method, clazz.getName());
              throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
                  + " has too many parameters");
          }
        }

        if (name.startsWith("visit")) {
          switch (parameters.length) {
            case 1:
              break;
            case 2:
              if (!stateClass.isAssignableFrom(parameters[1])) {
                logger.error("Method {} in {} has an invalid second parameter", method, clazz.getName());
                throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
                    + " has an invalid second parameter");
              }
              break;
            default:
              logger.error("Method {} in {} has too many parameters", method, clazz.getName());
              throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
                  + " has too many parameters");
          }

          if (outputClass == Void.class || outputClass == Void.TYPE) {
            if (method.getReturnType() != Void.class && method.getReturnType() != Void.TYPE) {
              logger.error("Method {} in {} has an invalid return type", method, clazz.getName());
              throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
                  + " has an invalid return type");
            }
          }

          if (method.getReturnType() == Void.class || method.getReturnType() == Void.TYPE) {
            if (outputClass != Void.class && outputClass != Void.TYPE) {
              logger.error("Method {} in {} has an invalid return type", method, clazz.getName());
              throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
                  + " has an invalid return type");
            }
          }
        }

        if (name.startsWith("postVisit")) {
          switch (parameters.length) {
            case 1:
              break;
            case 2:
              if (!stateClass.isAssignableFrom(parameters[1])) {
                logger.error("Method {} in {} has an invalid second parameter", method, clazz.getName());
                throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
                    + " has an invalid second parameter");
              }
              break;
            case 3:
              if (!outputClass.isAssignableFrom(parameters[1])) {
                logger.error("Method {} in {} has an invalid second parameter", method, clazz.getName());
                throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
                    + " has an invalid second parameter");
              }
              if (!stateClass.isAssignableFrom(parameters[2])) {
                logger.error("Method {} in {} has an invalid third parameter", method, clazz.getName());
                throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
                    + " has an invalid third parameter");
              }
              break;
            default:
              logger.error("Method {} in {} has too many parameters", method, clazz.getName());
              throw new IllegalArgumentException("Method " + method + " in " + clazz.getName()
                  + " has too many parameters");
          }
        }

        if (results.put(parameters[0], method) != null) {
          logger.warn("Duplicate method ({}) found in {} for visiting {}", new Object[] { method, clazz.getName(),
              parameters[0] });
          throw new IllegalArgumentException("Duplicate method (" + method + ") found in " + clazz.getName()
              + " for visiting " + parameters[0]);
        }
      }
    }
  }

  /**
   * Generate class.
   * 
   * @param profiled the profiled
   * @return the class
   * @throws CannotCompileException the cannot compile exception
   * @throws NotFoundException the not found exception
   */
  private Class<?> generateClass(final boolean profiled) throws CannotCompileException, NotFoundException {
    final ClassPool pool = new ClassPool(null);
    pool.appendSystemPath();
    pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));

    final CtClass cc = pool.makeClass(this.visitorClass.getName() + (profiled ? "Profiled" : "Unprofiled")
        + VisitorDelegator.class.getSimpleName(), pool.get(VisitorDelegator.class.getName()));

    // simple recurse
    {
      try (final StringBuilderWriter sbw = new StringBuilderWriter()) {
        try (final PrintWriter out = new PrintWriter(sbw)) {
          out.println("public final Object recurse(" + Visitor.class.getName()
              + " fastVisitor, Object input, Object state) {");
          out.println("  final " + visitorClass.getName() + " visitor = (" + visitorClass.getName() + ") fastVisitor;");
          generatePreVisitCode(out, profiled);
          generateVisitCode(out, profiled);
          generatePostVisitCode(out, profiled);
          out.println("  return result;");
          out.println("}");
        }

        final String methodeCode = sbw.toString();
        logger.debug("Generated {}.recurse(Object) code: {}", visitorClass.getName(), methodeCode);
        final CtMethod ctMethod = CtNewMethod.make(methodeCode, cc);
        ctMethod.setModifiers(ctMethod.getModifiers() & ~Modifier.ABSTRACT);
        cc.addMethod(ctMethod);
      }
    }

    // Object[] recurse(final Visitor visitor, VisitingMode visitongMode, final
    // Object[] objects, final
    // Object state);
    {
      try (final StringBuilderWriter sbw = new StringBuilderWriter()) {
        try (final PrintWriter out = new PrintWriter(sbw)) {

          out.println("public final Object[] recurseAll(" + Visitor.class.getName() + " visitor, "
              + VisitingMode.class.getName() + " visitingMode, Object[] objects, Object state) {");
          out.println("  if (" + VisitingMode.class.getName() + ".SEQUENTIAL.equals(visitingMode)) {");
          if (outputClass == Void.class || outputClass == Void.TYPE) {
            out.println("    return recurseSequentialVoidOutput(visitor, objects, state);");
          } else {
            out.println("    return recurseSequentialNonVoidOutput(visitor,objects,state);");
          }
          out.println("  } else {");
          if (outputClass == Void.class || outputClass == Void.TYPE) {
            out.println("    return recurseParallelVoidOutput(visitor, objects, state);");
          } else {
            out.println("    return recurseParallelNonVoidOutput(visitor,objects,state);");
          }
          out.println("  }");

          out.println("}");
        }

        final String methodeCode = sbw.toString();
        logger.debug("Generated {}.recurseAll(Object[]) code: {}", visitorClass.getName(), methodeCode);
        final CtMethod ctMethod = CtNewMethod.make(methodeCode, cc);
        ctMethod.setModifiers(ctMethod.getModifiers() & ~Modifier.ABSTRACT);
        cc.addMethod(ctMethod);
      }
    }

    // Object[] recurse(final Visitor visitor, VisitingMode visitongMode, final
    // Collection objects, final
    // Object state);
    {
      try (final StringBuilderWriter sbw = new StringBuilderWriter()) {
        try (final PrintWriter out = new PrintWriter(sbw)) {

          out.println("public final Object[] recurseAll(" + Visitor.class.getName() + " visitor, "
              + VisitingMode.class.getName() + " visitingMode, " + Collection.class.getName()
              + " objects, Object state) {");

          out.println("  if (" + VisitingMode.class.getName() + ".SEQUENTIAL.equals(visitingMode)) {");
          if (outputClass == Void.class || outputClass == Void.TYPE) {
            out.println("    return recurseSequentialVoidOutput(visitor, objects, state);");
          } else {
            out.println("    return recurseSequentialNonVoidOutput(visitor,objects,state);");
          }
          out.println("  } else {");
          if (outputClass == Void.class || outputClass == Void.TYPE) {
            out.println("    return recurseParallelVoidOutput(visitor, objects, state);");
          } else {
            out.println("    return recurseParallelNonVoidOutput(visitor,objects,state);");
          }
          out.println("  }");

          out.println("}");
        }

        final String methodeCode = sbw.toString();
        logger.debug("Generated {}.recurseAll(List) code: {}", visitorClass.getName(), methodeCode);
        final CtMethod ctMethod = CtNewMethod.make(methodeCode, cc);
        ctMethod.setModifiers(ctMethod.getModifiers() & ~Modifier.ABSTRACT);
        cc.addMethod(ctMethod);
      }
    }

    cc.setModifiers(cc.getModifiers() & ~Modifier.ABSTRACT | Modifier.FINAL);

    return cc.toClass();
  }

  /**
   * Generate postVisit code.
   * 
   * @param out the out
   * @param profiled the profiled
   */
  private void generatePostVisitCode(final PrintWriter out, final boolean profiled) {
    if (!postVisitMethodsStatistics.isEmpty()) {
      final String stateCode = "((" + stateClass.getName() + ") state)";
      final String outputCode = "((" + outputClass.getName() + ") result)";

      boolean hasManyCases = false;
      boolean first = true;
      for (int i = 0; i < postVisitMethodsStatistics.size(); ++i) {
        final MethodDescriptor ms = postVisitMethodsStatistics.get(i);
        if (profiled) {
          logger.info("postVisit call order for {}: {}", visitorClass.getName(), ms.method);
        }

        if (i == 0) {
          hasManyCases = !(postVisitMethodsStatistics.size() == 1 && inputClass
              .equals(ms.method.getParameterTypes()[0]));
        }

        if (first) {
          first = false;
        } else {
          out.print("  else ");
        }
        switch (ms.method.getParameterTypes().length) {
          case 1: {
            if (hasManyCases) {
              out.println("  if (input instanceof " + ms.method.getParameterTypes()[0].getName() + ") {");
            }
            if (profiled) {
              out.println("    postVisitCounters[" + i + "].incrementAndGet();");
            }
            out.println("    visitor." + ms.method.getName() + "( (" + ms.method.getParameterTypes()[0].getName()
                + ") input);");
            if (hasManyCases) {
              out.print("  }");
            }
          }
          case 2: {
            if (hasManyCases) {
              out.println("  if (input instanceof " + ms.method.getParameterTypes()[0].getName() + ") {");
            }
            if (profiled) {
              out.println("    postVisitCounters[" + i + "].incrementAndGet();");
            }
            out.println("    visitor." + ms.method.getName() + "( (" + ms.method.getParameterTypes()[0].getName()
                + ") input, " + outputCode + ");");
            if (hasManyCases) {
              out.print("  }");
            }
          }
          case 3: {
            if (hasManyCases) {
              out.println("  if (input instanceof " + ms.method.getParameterTypes()[0].getName() + ") {");
            }
            if (profiled) {
              out.println("    postVisitCounters[" + i + "].incrementAndGet();");
            }
            out.println("    visitor." + ms.method.getName() + "( (" + ms.method.getParameterTypes()[0].getName()
                + ") input, " + stateCode + ", " + outputCode + ");");
            if (hasManyCases) {
              out.print("  }");
            }
          }
        }
      }
      if (hasManyCases && !first) {
        out.println("  else throw new " + IllegalStateException.class.getName()
            + "(\"Unhandled type \"+input.getClass().getName()+\" for postVisit\");");
        out.println();
        out.println();
      }
    }
  }

  /**
   * Generate preVisit code.
   * 
   * @param out the out
   * @param profiled the profiled
   */
  private void generatePreVisitCode(final PrintWriter out, final boolean profiled) {
    if (!preVisitMethodsStatistics.isEmpty()) {
      final String stateCode = "((" + stateClass.getName() + ") state)";

      boolean hasManyCases = false;
      boolean first = true;
      for (int i = 0; i < preVisitMethodsStatistics.size(); ++i) {
        final MethodDescriptor ms = preVisitMethodsStatistics.get(i);
        if (profiled) {
          logger.info("preVisit call order for {}: {}", visitorClass.getName(), ms.method);
        }

        if (i == 0) {
          hasManyCases = !(preVisitMethodsStatistics.size() == 1 && inputClass.equals(ms.method.getParameterTypes()[0]));
        }

        if (first) {
          first = false;
        } else {
          out.print("  else ");
        }
        if (ms.method.getParameterTypes().length == 1) {
          if (hasManyCases) {
            out.println("  if (input instanceof " + ms.method.getParameterTypes()[0].getName() + ") {");
          }
          if (profiled) {
            out.println("    preVisitCounters[" + i + "].incrementAndGet();");
          }
          out.println("    visitor." + ms.method.getName() + "( (" + ms.method.getParameterTypes()[0].getName()
              + ") input);");
          if (hasManyCases) {
            out.print("  }");
          }
        } else {
          if (hasManyCases) {
            out.println("  if (input instanceof " + ms.method.getParameterTypes()[0].getName() + ") {");
          }
          if (profiled) {
            out.println("    preVisitCounters[" + i + "].incrementAndGet();");
          }
          out.println("    visitor." + ms.method.getName() + "( (" + ms.method.getParameterTypes()[0].getName()
              + ") input, " + stateCode + ");");
          if (hasManyCases) {
            out.print("  }");
          }
        }
      }
      if (hasManyCases && !first) {
        out.print("  else throw new " + IllegalStateException.class.getName()
            + "(\"Unhandled type \"+input.getClass().getName()+\" for preVisit\");");
        out.println();
        out.println();
      }
    }
  }

  /**
   * Generate visit code.
   * 
   * @param out the out
   * @param profiled the profiled
   */
  private void generateVisitCode(final PrintWriter out, final boolean profiled) {
    out.println("final " + outputClass.getName() + " result = null;");

    if (!visitMethodsStatistics.isEmpty()) {
      final String stateCode = "((" + stateClass.getName() + ") state)";

      boolean hasManyCases = false;
      boolean first = true;
      for (int i = 0; i < visitMethodsStatistics.size(); ++i) {
        final MethodDescriptor ms = visitMethodsStatistics.get(i);
        if (profiled) {
          logger.info("visit call order for {}: {}", visitorClass.getName(), ms.method);
        }

        if (i == 0) {
          hasManyCases = !(visitMethodsStatistics.size() == 1 && inputClass.equals(ms.method.getParameterTypes()[0]));
        }

        if (first) {
          first = false;
        } else {
          out.print("  else ");
        }

        if (outputClass == Void.class || outputClass == Void.TYPE) {
          if (ms.method.getParameterTypes().length == 1) {
            if (hasManyCases) {
              out.println("  if (input instanceof " + ms.method.getParameterTypes()[0].getName() + ") {");
            }
            if (profiled) {
              out.println("    visitCounters[" + i + "].incrementAndGet();");
            }
            out.println("    visitor." + ms.method.getName() + "( (" + ms.method.getParameterTypes()[0].getName()
                + ") input);");
            if (hasManyCases) {
              out.print("  }");
            }
          } else {
            if (hasManyCases) {
              out.println("  if (input instanceof " + ms.method.getParameterTypes()[0].getName() + ") {");
            }
            if (profiled) {
              out.println("    visitCounters[" + i + "].incrementAndGet();");
            }
            out.println("    visitor." + ms.method.getName() + "( (" + ms.method.getParameterTypes()[0].getName()
                + ") input, " + stateCode + ");");
            if (hasManyCases) {
              out.print("  }");
            }
          }
        } else {
          String preBoxing, postBoxing;
          final Class<?> returnType = ms.method.getReturnType();
          if (returnType.isPrimitive()) {
            // handle boxing of primitive types
            if (returnType == Boolean.TYPE) {
              preBoxing = "Boolean.valueOf(";
              postBoxing = ")";
            } else if (returnType == Character.TYPE) {
              preBoxing = "Character.valueOf(";
              postBoxing = ")";
            } else if (returnType == Byte.TYPE) {
              preBoxing = "Byte.valueOf(";
              postBoxing = ")";
            } else if (returnType == Short.TYPE) {
              preBoxing = "Short.valueOf(";
              postBoxing = ")";
            } else if (returnType == Integer.TYPE) {
              preBoxing = "Integer.valueOf(";
              postBoxing = ")";
            } else if (returnType == Long.TYPE) {
              preBoxing = "Long.valueOf(";
              postBoxing = ")";
            } else if (returnType == Float.TYPE) {
              preBoxing = "Float.valueOf(";
              postBoxing = ")";
            } else if (returnType == Double.TYPE) {
              preBoxing = "Double.valueOf(";
              postBoxing = ")";
            } else {
              throw new IllegalStateException("A programmatic error occurred");
            }
          } else {
            preBoxing = "";
            postBoxing = "";
          }

          if (ms.method.getParameterTypes().length == 1) {
            if (hasManyCases) {
              out.println("  if (input instanceof " + ms.method.getParameterTypes()[0].getName() + ") {");
            }

            if (profiled) {
              out.println("    visitCounters[" + i + "].incrementAndGet();");
            }
            out.println("    result = " + preBoxing + "visitor." + ms.method.getName() + "( ("
                + ms.method.getParameterTypes()[0].getName() + ") input)" + postBoxing + ";");
            if (hasManyCases) {
              out.print("  }");
            }
          } else {
            if (hasManyCases) {
              out.println("if (input instanceof " + ms.method.getParameterTypes()[0].getName() + ") {");
            }
            if (profiled) {
              out.println("    visitCounters[" + i + "].incrementAndGet();");
            }
            out.println("    result = " + preBoxing + "visitor." + ms.method.getName() + "( ("
                + ms.method.getParameterTypes()[0].getName() + ") input, " + stateCode + ")" + postBoxing + ";");
            if (hasManyCases) {
              out.print("  }");
            }
          }
        }
      }
      if (hasManyCases && !first) {
        out.println("  else throw new " + IllegalStateException.class.getName()
            + "(\"Unhandled type \"+input.getClass().getName()+\" for visit\");");
        out.println();
        out.println();
      }
    }
  }

  /**
   * Gets the postVisit methods statistics.
   * 
   * @param className the class name
   * @return the postVisit methods statistics
   */
  private MethodDescriptor getPostVisitMethodsStatistics(final String className) {
    for (final MethodDescriptor ms : postVisitMethodsStatistics) {
      if (className.equals(ms.className)) {
        return ms;
      }
    }
    return null;
  }

  /**
   * Gets the preVisit methods statistics.
   * 
   * @param className the class name
   * @return the preVisit methods statistics
   */
  private MethodDescriptor getPreVisitMethodsStatistics(final String className) {
    for (final MethodDescriptor ms : preVisitMethodsStatistics) {
      if (className.equals(ms.className)) {
        return ms;
      }
    }
    return null;
  }

  /**
   * Gets the visit methods statistics.
   * 
   * @param className the class name
   * @return the visit methods statistics
   */
  private MethodDescriptor getVisitMethodsStatistics(final String className) {
    for (final MethodDescriptor ms : visitMethodsStatistics) {
      if (className.equals(ms.className)) {
        return ms;
      }
    }
    return null;
  }

  /**
   * Load statistics from classpath or from statistics directory.
   */
  private void loadStatistics() {

    final String dirname = visitorClass.getPackage().getName().replace('.', File.separatorChar) + File.separatorChar;
    final String filename = visitorClass.getName() + ".visitorStatistics";

    // load from classpath
    boolean classpathStatistics = true;
    try {
      final Properties properties = new Properties();
      final URL url = getClass().getClassLoader().getResource(dirname + filename);
      if (url != null) {
        try (final InputStream is = new BufferedInputStream(url.openStream())) {
          properties.load(is);
        }
        loadStatistics(properties);
      } else {
        classpathStatistics = false;
      }
    } catch (final Exception e) {
      classpathStatistics = false;
    }
    if (!classpathStatistics) {
      for (final MethodDescriptor ms : preVisitMethodsStatistics) {
        ms.counter.set(0);
      }
      for (final MethodDescriptor ms : visitMethodsStatistics) {
        ms.counter.set(0);
      }
      for (final MethodDescriptor ms : postVisitMethodsStatistics) {
        ms.counter.set(0);
      }
    }

    // load from statistics directory
    try {
      final Properties properties = new Properties();
      try (final InputStream is = new BufferedInputStream(new FileInputStream(new File(
          Visitor.getStatisticsDirectory(), dirname + filename)))) {
        properties.load(is);
      }
      loadStatistics(properties);
    } catch (final Exception e) {
      for (final MethodDescriptor ms : preVisitMethodsStatistics) {
        ms.counter.set(0);
      }
      for (final MethodDescriptor ms : visitMethodsStatistics) {
        ms.counter.set(0);
      }
      for (final MethodDescriptor ms : postVisitMethodsStatistics) {
        ms.counter.set(0);
      }
    }

  }

  /**
   * Load statistics.
   * 
   * @param properties the properties
   */
  private void loadStatistics(final Properties properties) {
    // parse properties
    for (final Entry<Object, Object> entry : properties.entrySet()) {
      final String key = (String) entry.getKey();
      final String value = (String) entry.getValue();

      if (key.startsWith("preVisit.")) {
        final MethodDescriptor ms = getPreVisitMethodsStatistics(key.substring("preVisit.".length()));
        if (ms != null) {
          ms.counter.set(Integer.parseInt(value));
        }
      } else if (key.startsWith("visit.")) {
        final MethodDescriptor ms = getVisitMethodsStatistics(key.substring("visit.".length()));
        if (ms != null) {
          ms.counter.set(Integer.parseInt(value));
        }
      } else if (key.startsWith("postVisit.")) {
        final MethodDescriptor ms = getPostVisitMethodsStatistics(key.substring("postVisit.".length()));
        if (ms != null) {
          ms.counter.set(Integer.parseInt(value));
        }
      }
    }
  }

  /**
   * Normalize the counter between 0 and 10000.
   * 
   * @param methodsStatistics the methods statistics
   */
  private void normalize(final List<MethodDescriptor> methodsStatistics) {
    Integer min = Integer.MAX_VALUE;
    Integer max = Integer.MIN_VALUE;

    for (final MethodDescriptor ms : methodsStatistics) {
      final int value = Math.abs(ms.counter.get());
      min = Math.min(min, value);
      max = Math.max(max, value);
    }

    if (methodsStatistics.size() == 1) {
      for (final MethodDescriptor ms : methodsStatistics) {
        ms.counter.set(1);
      }
    } else {
      if (max - min != 0) {
        for (final MethodDescriptor ms : methodsStatistics) {
          ms.counter.set(10000 * (ms.counter.get() - min) / (max - min));
        }
      } else {
        for (final MethodDescriptor ms : methodsStatistics) {
          ms.counter.set(1);
        }
      }
    }
  }

  /**
   * Save statistics.
   */
  public synchronized void saveStatistics() {
    final Properties properties = new Properties();

    normalize(preVisitMethodsStatistics);
    normalize(visitMethodsStatistics);
    normalize(postVisitMethodsStatistics);

    for (final MethodDescriptor ms : preVisitMethodsStatistics) {
      properties.put("preVisit." + ms.className, Integer.toString(ms.counter.get()));
    }
    for (final MethodDescriptor ms : visitMethodsStatistics) {
      properties.put("visit." + ms.className, Integer.toString(ms.counter.get()));
    }
    for (final MethodDescriptor ms : postVisitMethodsStatistics) {
      properties.put("postVisit." + ms.className, Integer.toString(ms.counter.get()));
    }

    if (!properties.isEmpty()) {
      final String dirname = visitorClass.getPackage().getName().replace('.', File.separatorChar) + File.separatorChar;
      final String filename = visitorClass.getName() + ".visitorStatistics";
      final File dir = new File(Visitor.getStatisticsDirectory(), dirname);

      try {
        dir.mkdirs();
        try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(dir, filename)))) {
          properties.store(os, "Profiling statistics for " + visitorClass.getName());
        }
      } catch (final Exception e) {
        logger.warn("Can not save visitor statistics for " + visitorClass.getName() + " in " + new File(dir, filename),
            e);
      }
    }
  }
}
