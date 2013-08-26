/*
 * Copyright 2013 Raffael Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.raffael.guards.agent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import ch.raffael.guards.GuardsInternalError;
import ch.raffael.guards.definition.CheckNulls;
import ch.raffael.guards.definition.Guard;


/**
 * @startuml
 * hide methods
 * hide attributes
 *
 * ClientClass -right-> "1" CheckerStore
 * CheckerStore o--> "*" CheckerBridge
 * CheckerBridge "1" *--> "1" Invoker
 * Invoker o-right-> "1" Checker
 *
 * ClientClass ..> CheckerBridge: call
 * ClientClass ..> CheckerBridge: initialize
 * CheckerBridge ..> Invoker: create
 * CheckerBridge ..> Checker: create
 * CheckerBridge ..> Invoker: call
 * Invoker ..> Checker: call
 *
 * Transformer .right.> CheckerStore: create
 * Transformer ..> CheckerBridge: create
 *
 * class Invoker <<synthetic>>
 *
 * note right of CheckerBridge
 * The bridge will do the type
 * analysis to choose the checker
 * method to use.
 *
 * It's initialised in the static
 * block of the client class which
 * allows us to use reflection.
 * end note
 *
 * @enduml
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class CheckerBridge {

    private static final Map<String, Class> PRIMITIVES = ImmutableMap.<String, Class>builder()
            .put("int", int.class)
            .put("byte", byte.class)
            .put("short", short.class)
            .put("long", long.class)
            .put("float", float.class)
            .put("double", double.class)
            .put("char", char.class)
            .put("boolean", boolean.class)
            .build();
    private static final Predicate<java.lang.reflect.Method> CHECKER_METHOD = new Predicate<java.lang.reflect.Method>() {
        @SuppressWarnings("ObjectEquality")
        @Override
        public boolean apply(java.lang.reflect.Method input) {
            if ( !input.getName().equals("check") ) {
                return false;
            }
            if ( input.getReturnType() != boolean.class ) {
                return false;
            }
            if ( input.getParameterTypes().length != 1 ) {
                return false;
            }
            return true;
        }
    };
    private static final Predicate<CheckerMethod> NONPRIMITIVE_CHECKER = new Predicate<CheckerMethod>() {
        @Override
        public boolean apply(CheckerBridge.CheckerMethod input) {
            return !input.type.isPrimitive();
        }
    };
    private static Map<Class<?>, Class<?>> PRIMITIVE_CONVERSIONS = ImmutableMap.<Class<?>, Class<?>>builder()
            .put(int.class, long.class)
            .put(byte.class, short.class)
            .put(short.class, int.class)
            .put(float.class, double.class)
            .put(char.class, int.class)
            .build();

    private final Type type;
    private final Method method;
    private final String targetDescription;
    private final Type valueType;
    private final GuardHandle guardHandle;
    private final Annotation guardAnnotation;
    private final Guard.Type guardType;
    private final ClassLoader classLoader;
    private final Object initLock = new Object();
    private volatile Invoker invoker = null;
    private Guard.Checker checker = null;
    private Class valueClass = null;
    private boolean checkNulls = false;

    CheckerStore checkerStore;
    int index = -1;

    CheckerBridge(Type type, Method method, String targetDescription, Type valueType, Guard.Type guardType, Annotation guardAnnotation, GuardHandle guardHandle, ClassLoader classLoader) {
        this.type = type;
        this.method = method;
        this.targetDescription = targetDescription;
        this.valueType = valueType;
        this.guardHandle = guardHandle;
        this.guardAnnotation = guardAnnotation;
        this.guardType = guardType;
        this.classLoader = classLoader;
    }

    public String check(int value) {
        return invoker.check(value) ? null : violationMessage(value);
    }

    public String check(byte value) {
        return invoker.check(value) ? null : violationMessage(value);
    }

    public String check(short value) {
        return invoker.check(value) ? null : violationMessage(value);
    }

    public String check(long value) {
        return invoker.check(value) ? null : violationMessage(value);
    }

    public String check(float value) {
        return invoker.check(value) ? null : violationMessage(value);
    }

    public String check(double value) {
        return invoker.check(value) ? null : violationMessage(value);
    }

    public String check(String value) {
        return invoker.check(value) ? null : violationMessage(value);
    }

    public String check(char value) {
        return invoker.check(value) ? null : violationMessage(value);
    }

    public String check(boolean value) {
        return invoker.check(value) ? null : violationMessage(value);
    }

    public String check(Object value) {
        if ( value == null && !checkNulls ) {
            return null;
        }
        return invoker.check(value) ? null : violationMessage(value);
    }

    private String violationMessage(Object value) {
        StringBuilder buf = new StringBuilder(128);
        //buf.append("Violation of ")
        //        .append(guardAnnotation.getClass().getName())
        buf.append("@")
                .append(guardAnnotation.annotationType().getSimpleName())
                .append(" violation in method ")
                .append(type.getClassName())
                .append(".")
                .append(method.getName())
                .append('(');
        boolean firstParam = true;
        for ( Type t : method.getArgumentTypes() ) {
            if ( firstParam ) {
                firstParam = false;
            }
            else {
                buf.append(", ");
            }
            buf.append(t.getClassName());
        }
        buf.append("):\n")
                .append(targetDescription)
                .append(": ")
                .append(guardHandle.violationMessage(guardAnnotation))
                .append("\nValue: ")
                .append(value);
        return buf.toString();
    }

    boolean initialized() {
        return invoker != null;
    }

    void initialize() {
        if ( !initialized() ) {
            synchronized ( initLock ) {
                if ( !initialized() ) {
                    doInitialize();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void doInitialize() {
        try {
            valueClass = PRIMITIVES.get(valueType.getClassName());
            if ( valueClass == null ) {
                valueClass = Class.forName(valueType.getClassName(), false, classLoader);
            }
            Class<?> checkerClass = Class.forName(guardAnnotation.annotationType().getName() + "$Checker", false, guardAnnotation.getClass().getClassLoader());
            if ( !Guard.Checker.class.isAssignableFrom(checkerClass) ) {
                return;
            }
            Constructor<?> checkerCtor;
            try {
                checkerCtor = checkerClass.getConstructor(guardAnnotation.annotationType(), Guard.Type.class, Class.class);
            }
            catch ( NoSuchMethodException e ) {
                try {
                    checkerCtor = checkerClass.getConstructor(Annotation.class, Guard.Type.class, Class.class);
                }
                catch ( NoSuchMethodException e2 ) {
                    e.addSuppressed(e2);
                    throw e;
                }
            }
            LinkedList<CheckerMethod> checkerMethods = matchCheckerMethods(valueClass, (Class<? extends Guard.Checker>)checkerClass);
            if ( checkerMethods.isEmpty() ) {
                Log.error("No matching candidate found in checker %s for value type %s", checkerClass.getName(), valueClass.getName());
                return;
            }
            else if ( checkerMethods.size() > 1 ) {
                Log.error("Multiple candidates found in checker %s for value type %s: %s", checkerClass.getName(), valueClass.getName(), checkerMethods);
                return;
            }
            checker = (Guard.Checker)checkerCtor.newInstance(guardAnnotation, guardType, valueClass);
            if ( checkerMethods.getFirst().method.getAnnotation(CheckNulls.class) != null ) {
                checkNulls = true;
            }
            Class<? extends Invoker> invokerClass = ClassSynthesizer.get(checkerClass)
                    .invokerClass((Class<? extends Guard.Checker>)checkerClass, checkerMethods.getFirst());
            invoker = invokerClass.getConstructor(checkerClass).newInstance(checker);
        }
        catch ( Exception e ) {
            // FIXME: Handle exception
            e.printStackTrace();
        }
        finally {
            if ( invoker == null ) {
                // an error occurred; make sure the code still runs, but without any checks
                checkerStore.invalidate(index);
            }
        }
    }

    @SuppressWarnings("ObjectEquality")
    private static LinkedList<CheckerMethod> matchCheckerMethods(Class<?> type, Class<? extends Guard.Checker> checkerClass) {
        LinkedList<CheckerMethod> methods = new LinkedList<>(
                Collections2.transform(
                        Collections2.filter(
                                Arrays.asList(checkerClass.getMethods()), CHECKER_METHOD),
                new Function<java.lang.reflect.Method, CheckerMethod>() {
                    @Override
                    public CheckerMethod apply(java.lang.reflect.Method input) {
                        return new CheckerMethod(input, input.getParameterTypes()[0]);
                    }
                }));
        if ( type.isPrimitive() ) {
            findPrimitiveChecker(methods, type, checkerClass);
            return methods;
        }
        // Pass 1: remove all that aren't applicable
        Iterator<CheckerMethod> iter = methods.iterator();
        while ( iter.hasNext() ) {
            CheckerMethod m = iter.next();
            if ( !m.type.isAssignableFrom(type) ) {
                iter.remove();
            }
        }
        // Pass 2: keep the most specific ones
        //
        // The JLS has a very interesting sentence about this problem:
        //
        //    The informal intuition is that one method is more specific than another if
        //    any invocation handled by the first method could be passed on to the other
        //    one without a compile-time type error.
        //
        // This is the approach applied here.
        //
        // FIXME: handle primitives:
        //   * byte -> short -> int -> long
        //   * float -> double
        // (what about int->double etc? just follow the JLS?)
        //
        // FIXME: Handle auto-boxing?
        //
        // We're iterating a copy on the outer loop to avoid
        // ConcurrentModificationExceptions because of remove() in the inner loop.
        for ( CheckerMethod specific : methods.toArray(new CheckerMethod[methods.size()]) ) {
            if ( !methods.contains(specific) ) {
                // has been removed already
                continue;
            }
            Iterator<CheckerMethod> otherIter = methods.iterator();
            while ( otherIter.hasNext() ) {
                CheckerMethod other = otherIter.next();
                if ( other == specific ) {
                    continue;
                }
                if ( other.type.isAssignableFrom(specific.type) ) {
                    otherIter.remove();
                }
            }
        }
        return methods; // expecting exactly size 1
    }

    @SuppressWarnings("ObjectEquality")
    private static void findPrimitiveChecker(LinkedList<CheckerMethod> methods, Class<?> type, Class<? extends Guard.Checker> checkerClass) {
        Iterators.removeIf(methods.iterator(), NONPRIMITIVE_CHECKER);
        while ( true ) {
            for ( CheckerMethod m : methods ) {
                if ( m.type == type ) {
                    methods.clear();
                    methods.add(m);
                    return;
                }
            }
            type = PRIMITIVE_CONVERSIONS.get(type);
            if ( type == null ) {
                methods.clear();
                return;
            }
        }
    }

    static class CheckerMethod {
        final java.lang.reflect.Method method;
        final Class<?> type;
        private CheckerMethod(java.lang.reflect.Method method, Class<?> type) {
            this.method = method;
            this.type = type;
        }
    }

    static public abstract class Invoker {
        public boolean check(int value) {
            throw new GuardsInternalError("check(int) not invokable");
        }
        public boolean check(byte value) {
            throw new GuardsInternalError("check(byte) not invokable");
        }
        public boolean check(short value) {
            throw new GuardsInternalError("check(short) not invokable");
        }
        public boolean check(long value) {
            throw new GuardsInternalError("check(long) not invokable");
        }
        public boolean check(float value) {
            throw new GuardsInternalError("check(float) not invokable");
        }
        public boolean check(double value) {
            throw new GuardsInternalError("check(double) not invokable");
        }
        public boolean check(char value) {
            throw new GuardsInternalError("check(char) not invokable");
        }
        public boolean check(boolean value) {
            throw new GuardsInternalError("check(boolean) not invokable");
        }
        public boolean check(Object value) {
            throw new GuardsInternalError("check(Object) not invokable");
        }
    }

}
