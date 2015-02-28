/*
 * Copyright 2015 Raffael Herzog
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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.guava.base.Function;
import ch.raffael.guards.agent.guava.base.Joiner;
import ch.raffael.guards.agent.guava.base.Supplier;
import ch.raffael.guards.agent.guava.base.Suppliers;
import ch.raffael.guards.agent.guava.collect.AbstractSequentialIterator;
import ch.raffael.guards.agent.guava.collect.ImmutableSet;
import ch.raffael.guards.agent.guava.collect.Iterables;
import ch.raffael.guards.agent.guava.primitives.Primitives;
import ch.raffael.guards.agent.guava.reflect.TypeToken;
import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.Guard.Handler;
import ch.raffael.guards.definition.HandlerPackage;
import ch.raffael.guards.definition.Message;
import ch.raffael.guards.definition.Positioning;
import ch.raffael.guards.definition.Relations;
import ch.raffael.guards.runtime.GuardsInternalError;
import ch.raffael.guards.runtime.IllegalGuardError;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class GuardDefinition {

    private static final ClassValue<GuardDefinition> DEFINITIONS = new ClassValue<GuardDefinition>() {
        @Override
        protected GuardDefinition computeValue(Class<?> type) {
            if ( !type.isAnnotation() ) {
                return null;
            }
            return createGuardDefinition(asAnnotation(type));
        }
    };

    private final Class<? extends Annotation> type;
    private final Guard guard;
    @SuppressWarnings("UnusedDeclaration")
    private final Message message;
    @SuppressWarnings("UnusedDeclaration")
    private final Relations relations;
    @SuppressWarnings("UnusedDeclaration")
    private final Positioning positioning;
    private final Set<Annotation> implied;
    private final Set<TestMethod> testMethods;

    private final Class<? extends Handler> handlerClass;
    private final Supplier<HandlerInstantiator> handlerInstantiator = Suppliers.memoize(new Supplier<HandlerInstantiator>() {
        @Override
        public HandlerInstantiator get() {
            return createInstantiator();
        }
    });

    public static GuardDefinition get(Class<?> type) {
        return DEFINITIONS.get(type);
    }

    private GuardDefinition(
            @NotNull Class<? extends Annotation> type,
            @Nullable Guard guard,
            @Nullable Message message,
            @Nullable Relations relations,
            @Nullable Positioning positioning,
            @NotNull Set<Annotation> implied)
    {
        this.type = type;
        this.guard = guard;
        this.message = message;
        this.relations = relations;
        this.positioning = positioning;
        this.implied = ImmutableSet.copyOf(implied);
        handlerClass = findHandlerClass();
        testMethods = findTestMethods();
    }

    private static GuardDefinition createGuardDefinition(Class<? extends Annotation> type) {
        try {
            if ( !new GuardAnnotationInspector(asAnnotation(type)).isGuard() ) {
                return null;
            }
        }
        catch ( GuardAnnotationInspector.Circularity circularity ) {
            throw new IllegalGuardError("Guard circularity detected: " + Joiner.on(" -> ")
                    .join(Iterables.transform(circularity.path, new Function<Class<? extends Annotation>, String>() {
                        @Override
                        public String apply(@Nullable Class<? extends Annotation> input) {
                            if ( input != null ) {
                                return input.getName();
                            }
                            else {
                                return null;
                            }
                        }
                    })));
        }
        Guard guard = null;
        Message message = null;
        Relations relations = null;
        Positioning positioning = null;
        Set<Annotation> implied = new LinkedHashSet<>();
        for( Annotation annotation : type.getAnnotations() ) {
            if ( annotation instanceof Guard ) {
                guard = (Guard)annotation;
            }
            else if ( annotation instanceof Message ) {
                message = (Message)annotation;
            }
            else if ( annotation instanceof Relations ) {
                relations = (Relations)annotation;
            }
            else if ( annotation instanceof Positioning ) {
                positioning = (Positioning)annotation;
            }
            else if ( DEFINITIONS.get(annotation.annotationType()) != null) {
                implied.add(annotation);
            }
        }
        if ( implied.isEmpty() && guard == null ) {
            // TODO: this can't actually happen after using GuardAnnotationCircularityDetector
            return null;
        }
        else {
            return new GuardDefinition(type, guard, message, relations, positioning, implied);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Class<? extends Handler> findHandlerClass() {
        if ( guard == null ) {
            return null;
        }
        Class handlerClass;
        if ( guard.handler() == Guard.ByConvention.class ) {
            handlerClass = null;
            for( Class<?> candidate : type.getDeclaredClasses() ) {
                if ( "Handler".equals(candidate.getSimpleName()) && Handler.class.isAssignableFrom(candidate) ) {
                    handlerClass = candidate;
                }
            }
            // TODO: remove this
            if ( handlerClass == null && type.getPackage() != null ) {
                HandlerPackage handlerPackage = type.getPackage().getAnnotation(HandlerPackage.class);
                if ( handlerPackage != null ) {
                    try {
                        handlerClass = Class.forName(
                                handlerPackage.value() + "."
                                        + type.getName().substring(type.getName().lastIndexOf('.') + 1) + "Handler",
                                false, type.getClassLoader());
                    }
                    catch ( ClassNotFoundException e ) {
                        // ignore
                    }
                }
            }
        }
        else {
            handlerClass = guard.handler();
        }
        if ( handlerClass == null ) {
            throw new IllegalGuardError("No suitable handler found for guard type " + type.getName());
        }
        if ( !Handler.class.isAssignableFrom(handlerClass) ) {
            throw new IllegalGuardError("Handler class " + handlerClass.getName() + " does not extend " + Handler.class.getCanonicalName());
        }
        return handlerClass;
    }

    @NotNull
    private Set<TestMethod> findTestMethods() {
        if ( handlerClass == null || handlerClass == Guard.AlwaysTrue.class ) {
            return ImmutableSet.of();
        }
        ImmutableSet.Builder<TestMethod> testMethods = ImmutableSet.builder();
        for( Method method : handlerClass.getMethods() ) {
            if ( method.getName().equals("test") || HandlerAccess.annotatedAsTestMethod(method) ) {
                Type[] parameterTypes = method.getGenericParameterTypes();
                if ( parameterTypes.length != 1 ) {
                    throw new IllegalGuardError(method + ": Test methods must take exactly one argument");
                }
                testMethods.add(new TestMethod(method, TypeToken.of(parameterTypes[0])));
            }
        }
        return testMethods.build();
    }

    private HandlerInstantiator createInstantiator() {
        if ( Modifier.isAbstract(handlerClass.getModifiers()) ) {
            throw new IllegalGuardError("Handler class " + handlerClass.getName() + " is abstract");
        }
        Constructor[] constructors = handlerClass.getConstructors();
        if ( constructors.length == 1 ) {
            return new HandlerInstantiator(constructors[0]);
        }
        else {
            Constructor constructor = null;
            for( Constructor candidate : constructors ) {
                if ( HandlerAccess.annotatedAsRuntimeConstructor(candidate) ) {
                    if ( constructor != null ) {
                        throw new IllegalGuardError("Multiple public constructors annotated with @RuntimeConstructor");
                    }
                    constructor = candidate;
                }
            }
            if ( constructor == null ) {
                throw new IllegalGuardError("No suitable constructor found for " + handlerClass);
            }
            return new HandlerInstantiator(constructor);
        }
    }

    @Nullable
    MethodHandle resolveTestMethod(@NotNull GuardInstance instance, @Nullable MethodHandle prependTo) {
        for( Annotation ann : implied ) {
            GuardInstance inst = new GuardInstance(instance, ann);
            prependTo = GuardDefinition.get(ann.annotationType()).resolveTestMethod(inst, prependTo);
        }
        if ( handlerClass == Guard.AlwaysTrue.class || guard == null ) {
            return prependTo;
        }
        TestMethod testMethod = findTestMethod(instance);
        if ( testMethod == null ) {
            throw new IllegalGuardError(instance.getTarget() + ": No matching test method found for " + handlerClass.getName());
        }
        instance.updateTestMethod(testMethod.method);
        MethodHandle handle;
        try {
            handle = MethodHandles.publicLookup().unreflect(testMethod.method);
        }
        catch ( IllegalAccessException e ) {
            throw new GuardsInternalError("Access to method " + testMethod.method + " unexpectedly denied");
        }
        if ( !Modifier.isStatic(testMethod.method.getModifiers()) ) {
            handle = handle.bindTo(handlerInstantiator.get().instantiate(instance.getAnnotation(), instance.getTarget().getGenericValueType()));
        }
        if ( !testMethod.valueType.isPrimitive() && !guard.testNulls() ) {
            handle = MethodHandles.guardWithTest(
                    Indy.testNotNullHandle(testMethod.valueType.getRawType()),
                    handle,
                    Indy.alwaysTrueHandle(testMethod.valueType.getRawType()));
        }
        return Indy.prependGuardMethod(handle, instance, prependTo);
    }

    @Nullable
    TestMethod findTestMethod(@NotNull GuardInstance instance) {
        assert guard != null;
        GuardTarget target = instance.getTarget();
        // TODO: can we optimise this? should we?
        TestMethod testMethod = null;
        if ( !instance.getTarget().getValueType().isPrimitive() ) {
            // we can safely skip this step when dealing with primitives
            // however, we still check for primitive wrappers
            testMethod = findForComplexType(instance.getTarget().getGenericValueType(), instance);
        }
        if ( testMethod == null && target.getValueType().isPrimitive() ) {
            // try widening the primitives
            testMethod = findWithPrimitiveConversions(target.getValueType(), instance);
        }
        if ( testMethod == null && !guard.testNulls() && Primitives.isWrapperType(target.getValueType()) ) {
            // try unboxing the value and then widening the primitive
            testMethod = findWithPrimitiveConversions(Primitives.unwrap(target.getValueType()), instance);
        }
        return testMethod;
    }

    @Nullable
    private TestMethod findForPrimitive(@NotNull Class<?> type, @NotNull GuardInstance instance) {
        assert type.isPrimitive();
        TestMethod testMethod = null;
        for( TestMethod candidate : testMethods ) {
            if ( candidate.valueType.getRawType() == type ) {
                if ( testMethod != null ) {
                    // ambiguity detected!
                    // TODO: how to resolve this?
                    throw new IllegalGuardError(instance.getTarget() + ": Ambiguous test method: Both " + testMethod.method + " and " + candidate.method + " match");
                }
                testMethod = candidate;
            }
        }
        return testMethod;
    }

    @Nullable
    private TestMethod findForComplexType(@NotNull TypeToken<?> type, @NotNull GuardInstance instance) {
        TestMethod testMethod = null;
        for( TestMethod candidate : testMethods ) {
            if ( candidate.valueType.isAssignableFrom(type) ) {
                if ( testMethod == null ) {
                    testMethod = candidate;
                }
                else {
                    if ( testMethod.valueType.isAssignableFrom(candidate.valueType) ) {
                        // the more specific one wins
                        testMethod = candidate;
                    }
                    else {
                        // ambiguity detected!
                        // TODO: how to resolve this?
                        throw new IllegalGuardError(instance.getTarget() + ": Ambiguous test method: Both " + testMethod.method + " and " + candidate.method + " match");
                    }
                }
            }
        }
        return testMethod;
    }

    private TestMethod findWithPrimitiveConversions(@NotNull Class<?> tryWithType, @NotNull GuardInstance instance) {
        assert tryWithType.isPrimitive();
        assert tryWithType != void.class;
        TestMethod testMethod = findForPrimitive(tryWithType, instance);
        if ( testMethod != null ) {
            return testMethod;
        }
        for( PrimitiveType type : Iterables.skip(PrimitiveType.forType(tryWithType), 1) ) {
            testMethod = findForPrimitive(type.type(), instance);
            if ( testMethod != null ) {
                return testMethod;
            }
        }
        return null;
    }

    private static enum PrimitiveType implements Iterable<PrimitiveType> {
        BYTE(byte.class) {
            @Nullable
            @Override
            PrimitiveType next() {
                return SHORT;
            }
        },
        SHORT(short.class) {
            @Nullable
            @Override
            PrimitiveType next() {
                return INT;
            }
        }, INT(int.class) {
            @Nullable
            @Override
            PrimitiveType next() {
                return LONG;
            }
        },
        LONG(long.class) {
            @Nullable
            @Override
            PrimitiveType next() {
                return null;
            }
        },
        FLOAT(float.class) {
            @Nullable
            @Override
            PrimitiveType next() {
                return DOUBLE;
            }
        },
        DOUBLE(double.class) {
            @Nullable
            @Override
            PrimitiveType next() {
                return null;
            }
        },
        CHAR(char.class) {
            @Nullable
            @Override
            PrimitiveType next() {
                return INT;
            }
        },
        BOOLEAN(boolean.class) {
            @Nullable
            @Override
            PrimitiveType next() {
                return null;
            }
        };

        private final Class<?> type;

        PrimitiveType(Class<?> type) {
            this.type = type;
        }

        static PrimitiveType forType(Class<?> type) {
            for( PrimitiveType t : values() ) {
                if ( t.type() == type ) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Not a primitive type: " + type);
        }

        @Nullable
        abstract PrimitiveType next();

        @NotNull
        Class<?> type() {
            return type;
        }

        public Iterator<PrimitiveType> iterator() {
            return new AbstractSequentialIterator<PrimitiveType>(this) {
                @Override
                protected PrimitiveType computeNext(PrimitiveType previous) {
                    return previous.next();
                }
            };
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Annotation> asAnnotation(Class<?> type) {
        if ( !type.isAnnotation() ) {
            throw new IllegalArgumentException(type + " is not an annotation");
        }
        return (Class<? extends Annotation>)type;
    }

    private static final Object[] NO_ARGS = new Object[0];
    private class HandlerInstantiator {

        private final Constructor constructor;
        private final Class[] paramTypes;

        public HandlerInstantiator(Constructor constructor) {
            this.constructor = constructor;
            paramTypes = constructor.getParameterTypes();
            for( Class t : paramTypes ) {
                getArgument(t, null, null);
            }
        }

        private Object getArgument(Class parameterType, Annotation annotation, TypeToken<?> targetType) {
            if ( parameterType == Class.class ) {
                if ( targetType == null ) {
                    return null;
                }
                return targetType.getRawType();
            }
            else if ( parameterType == Type.class ) {
                return targetType;
            }
            else if ( parameterType == type ) {
                return annotation;
            }
            else {
                throw new IllegalGuardError("Cannot inject constructor argument of type " + parameterType);
            }
        }

        private Object[] getArguments(Annotation annotation, TypeToken targetType) {
            Object[] arguments = new Object[paramTypes.length];
            for( int i = 0; i < paramTypes.length; i++ ) {
                arguments[i] = getArgument(paramTypes[i], annotation, targetType);
            }
            return arguments;
        }

        Handler instantiate(Annotation annotation, TypeToken targetType) {
            try {
                return (Handler)constructor.newInstance(paramTypes.length > 0 ? getArguments(annotation, targetType) : NO_ARGS);
            }
            catch ( InstantiationException | IllegalAccessException | InvocationTargetException e ) {
                throw new GuardsInternalError("Error invoking " + constructor);
            }
        }

    }

    static final class TestMethod {
        private final TypeToken<?> valueType;
        private final Method method;
        private TestMethod(Method method, TypeToken<?> valueType) {
            this.valueType = valueType;
            this.method = method;
        }
        Type valueType() {
            return valueType.getType();
        }
        Method method() {
            return method;
        }
    }

    private final static class HandlerAccess extends Handler {
        private HandlerAccess() {
        }
        static boolean annotatedAsRuntimeConstructor(@NotNull Constructor constructor) {
            return constructor.getAnnotation(RuntimeConstructor.class) != null;
        }
        static boolean annotatedAsTestMethod(@NotNull Method method) {
            return method.getAnnotation(Test.class) != null;
        }
    }

}
