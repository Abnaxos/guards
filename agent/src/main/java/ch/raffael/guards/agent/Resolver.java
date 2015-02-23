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
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.guava.base.MoreObjects;
import ch.raffael.guards.agent.guava.collect.AbstractSequentialIterator;
import ch.raffael.guards.agent.guava.collect.ImmutableList;
import ch.raffael.guards.agent.guava.collect.Iterables;
import ch.raffael.guards.agent.guava.primitives.Primitives;
import ch.raffael.guards.agent.guava.reflect.TypeToken;
import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.Guard.Handler;
import ch.raffael.guards.definition.Guard.TypeConversions;
import ch.raffael.guards.definition.HandlerPackage;
import ch.raffael.guards.runtime.GuardsInternalError;
import ch.raffael.guards.runtime.IllegalGuardError;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Resolver {

    private static final ClassValue<Resolver> RESOLVERS = new ClassValue<Resolver>() {
        @SuppressWarnings("unchecked")
        @Override
        @NotNull
        protected Resolver computeValue(@NotNull Class<?> type) {
            Guard guardAnnotation = type.getAnnotation(Guard.class);
            if ( !type.isAnnotation() || guardAnnotation == null ) {
                throw new GuardsInternalError("Resolver requested for non-guard annotation " + type);
            }
            assert type.getTypeParameters().length == 0; // just make sure; the JLS (currently) requires this
            Class<?> handlerClass = guardAnnotation.handler();
            if ( !Handler.class.isAssignableFrom(handlerClass) ) {
                throw new IllegalGuardError(handlerClass + " does not extend " + Handler.class.getCanonicalName());
            }
            if ( handlerClass == Guard.AlwaysTrue.class ) {
                return new AlwaysTrueResolver((Class<? extends Annotation>)type);
            }
            else if ( handlerClass == Guard.ByConvention.class ) {
                handlerClass = findHandlerByConvention(type);
                if ( handlerClass == null ) {
                    throw new IllegalGuardError("Could not find any handler for guard " + type.getName() + " by convention");
                }
            }
            return new Resolver((Class<? extends Annotation>)type, (Class<? extends Handler<?>>)handlerClass);
        }

        @Nullable
        private Class<? extends Handler<?>> findHandlerByConvention(@NotNull Class<?> type) {
            Class<? extends Handler<?>> handlerType = tryLoadHandler(type, type.getName() + "$Handler");
            if ( handlerType != null ) {
                if ( !type.equals(handlerType.getEnclosingClass()) ) {
                    Logging.LOG.warning(handlerType + " is not an inner class of " + type);
                    handlerType = null;
                }
            }
            if ( handlerType == null ) {
                if ( type.getSimpleName().equals("") ) {
                    return null;
                }
                Package pkg = type.getPackage();
                if ( pkg == null ) {
                    Logging.LOG.warning("No package defined for class " + type);
                    return null;
                }
                HandlerPackage searchPackage = pkg.getAnnotation(HandlerPackage.class);
                if ( searchPackage == null ) {
                    return null;
                }
                handlerType = tryLoadHandler(type, searchPackage.value() + "." + type.getSimpleName() + "Handler");
            }
            return handlerType;
        }

        @SuppressWarnings("unchecked")
        private Class<? extends Handler<?>> tryLoadHandler(Class<?> type, String name) {
            try {
                Class<?> handlerType = Class.forName(name, false, type.getClassLoader());
                if ( !Handler.class.isAssignableFrom(handlerType) ) {
                    return null;
                }
                return (Class<? extends Handler<?>>)handlerType;
            }
            catch ( ClassNotFoundException e ) {
                return null;
            }
        }

    };

    private final Class<? extends Annotation> guardType;
    private final boolean testNulls;
    @SuppressWarnings("UnusedDeclaration")
    private final Class<? extends Handler<?>> handlerType;
    private final TypeConversions conversions;
    private final Constructor<? extends Handler<?>> constructor;
    private final List<TestMethod> testMethods;

    @SuppressWarnings("unchecked")
    Resolver(@NotNull Class<? extends Annotation> guardType, @NotNull Class<? extends Handler<?>> handlerType) {
        this.guardType = guardType;
        this.handlerType = handlerType;
        testNulls = (guardType.getAnnotation(Guard.class).testNulls());
        this.constructor = (Constructor<? extends Handler<?>>)findConstructor(guardType, handlerType);
        ImmutableList.Builder<TestMethod> testMethods = ImmutableList.builder();
        for( Method method : handlerType.getMethods() ) {
            // TODO: non-public methods annotated with @Handler.Test will be undetected
            Type[] parameterTypes = method.getGenericParameterTypes();
            if ( parameterTypes.length != 1 ) {
                if ( method.getAnnotation(Handler.Test.class) != null ) {
                    throw new IllegalGuardError("Illegal test method: " + method);
                }
            }
            else if ( method.getName().equals("test") || method.getAnnotation(Handler.Test.class) != null ) {
                try {
                    testMethods.add(new TestMethod(TypeToken.of(parameterTypes[0]), method));
                }
                catch ( IllegalAccessException e ) {
                    throw new GuardsInternalError("Non-public test method found: " + method, e);
                }
            }
        }
        this.testMethods = testMethods.build();
        this.conversions = MoreObjects.firstNonNull(guardType.getAnnotation(TypeConversions.class), TypeConversions.DEFAULTS);
    }

    /**
     * Constructor for AlwaysTrue
     */
    private Resolver(Class<? extends Annotation> guardType) {
        this.guardType = guardType;
        this.testNulls = false;
        this.handlerType = Guard.AlwaysTrue.class;
        this.conversions = TypeConversions.DEFAULTS;
        this.constructor = null;
        this.testMethods = null;
    }

    static Resolver resolverFor(Class<?> guardType) {
        return RESOLVERS.get(guardType);
    }

    @NotNull
    private Constructor<?> findConstructor(@NotNull Class<? extends Annotation> guardType, @NotNull Class<? extends Handler<?>> handlerType) {
        Constructor<?> constructor = null;
        for( Constructor<?> candidate : handlerType.getConstructors() ) {
            Class<?>[] candidateParameterTypes = candidate.getParameterTypes();
            if ( candidateParameterTypes.length == 0 ) {
                if ( constructor == null ) {
                    constructor = candidate;
                }
            }
            else if ( candidateParameterTypes.length == 1 ) {
                Class<?> paramType = candidate.getParameterTypes()[0];
                if ( paramType.isAssignableFrom(guardType) ) {
                    assert paramType.getTypeParameters().length == 0; // just make sure; the JLS (currently) requires this
                    if ( constructor != null ) {
                        Class<?>[] currentParamTypes = constructor.getParameterTypes();
                        if ( currentParamTypes.length == 0 ) {
                            // the more eager constructor wins
                            constructor = candidate;
                        }
                        else if ( currentParamTypes[0].isAssignableFrom(paramType) ) {
                            // the 'nearest' parameter type wins
                            constructor = candidate;
                        }
                    }
                    else {
                        constructor = candidate;
                    }
                }
            }
        }
        if ( constructor == null ) {
            throw new IllegalGuardError("No suitable constructor for " + guardType.getName() + " found in handler " + handlerType);
        }
        return constructor;
    }

    @NotNull
    private Handler<?> createHandler(@NotNull Annotation annotation) {
        if ( annotation.annotationType() != guardType ) {
            throw new IllegalArgumentException("Annotation of type " + guardType.getName() + " expected (got " + annotation.getClass() + ")");
        }
        try {
            if ( constructor.getParameterTypes().length == 0 ) {
                return constructor.newInstance();
            }
            else {
                return constructor.newInstance(annotation);
            }
        }
        catch ( InstantiationException | IllegalAccessException e ) {
            throw new GuardsInternalError("Error creating new handler instance for " + annotation, e);
        }
        catch ( InvocationTargetException e ) {
            // TODO: we might want to handle this more gracefully, this might be part of the guard definition
            assert e.getCause() != null;
            if ( e.getCause() instanceof IllegalGuardError ) {
                throw (IllegalGuardError)e.getCause();
            }
            throw new GuardsInternalError("Error creating new handler instance for " + annotation, e.getCause());
        }
    }

    @NotNull
    TestMethod findTestMethod(@NotNull GuardInstance instance) {
        GuardTarget target = instance.getTarget();
        // TODO: can we optimise this? should we?
        TestMethod testMethod = null;
        if ( !instance.getTarget().getValueType().isPrimitive() ) {
            // we can safely skip this step when dealing with primitives
            // however, we still check for primitive wrappers
            testMethod = findForComplexType(TypeToken.of(instance.getTarget().getGenericValueType()), instance);
        }
        if ( testMethod == null && target.getValueType().isPrimitive() ) {
            // try widening the primitives
            testMethod = findWithPrimitiveConversions(target.getValueType(), instance);
        }
        if ( testMethod == null && conversions.unbox() && Primitives.isWrapperType(target.getValueType()) ) {
            // try unboxing the value and then widening the primitive
            testMethod = findWithPrimitiveConversions(Primitives.unwrap(target.getValueType()), instance);
            if ( testNulls ) {
                // TODO: find something better to do here? IllegalGuardError?
                Log.warning("Unwrapping " + target.getValueType() + " on handler that checks null values");
            }
        }
        if ( testMethod == null ) {
            throw new IllegalGuardError(target + ": No matching test method found for " + guardType.getName());
        }
        // make sure our value types match
        testMethod = testMethod.forValueType(target.getValueType());
        MethodHandle methodHandle = testMethod.methodHandle();
        if ( !Modifier.isStatic(testMethod.method.getModifiers()) ) {
            methodHandle = methodHandle.bindTo(createHandler(instance.getAnnotation()));
        }
        if ( !testNulls && !target.getValueType().isPrimitive() ) {
            methodHandle = MethodHandles.guardWithTest(
                    Indy.testNotNullHandle(target.getValueType()),
                    methodHandle, Indy.alwaysTrueHandle(target.getValueType()));
        }
        return testMethod.forHandle(methodHandle);
    }

    @Nullable
    private TestMethod findForPrimitive(@NotNull Class<?> type, @NotNull GuardInstance instance) {
        assert type.isPrimitive();
        TestMethod testMethod = null;
        for( TestMethod candidate : testMethods ) {
            if ( candidate.valueType() == type ) {
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
            if ( type == PrimitiveType.LONG && !conversions.widenToLong() ) {
                break;
            }
            else if ( type == PrimitiveType.DOUBLE && !conversions.widenToDouble() ) {
                break;
            }
            testMethod = findForPrimitive(type.type(), instance);
            if ( testMethod != null ) {
                return testMethod;
            }
        }
        return null;
    }

    static final class TestMethod {
        private final TypeToken<?> valueType;
        private final Method method;
        private final MethodHandle methodHandle;
        private TestMethod(TypeToken<?> valueType, Method method) throws IllegalAccessException {
            this(valueType, method, MethodHandles.publicLookup().unreflect(method));
        }
        private TestMethod(TypeToken<?> valueType, Method method, MethodHandle handle) {
            this.valueType = valueType;
            this.method = method;
            this.methodHandle = handle;
        }
        Type valueType() {
            return valueType.getType();
        }
        Method method() {
            return method;
        }
        MethodHandle methodHandle() {
            return methodHandle;
        }

        private TestMethod forHandle(MethodHandle handle) {
            return new TestMethod(valueType, method, handle);
        }

        TestMethod forValueType(Class<?> valueType) {
            MethodType methodType = methodHandle.type();
            if ( !methodType.parameterType(methodType.parameterCount() - 1).equals(valueType) ) {
                return forHandle(methodHandle.asType(methodType.changeParameterType(methodType.parameterCount() - 1, valueType)));
            }
            else {
                return this;
            }
        }
    }

    private static final class AlwaysTrueResolver extends Resolver {

        private static final Method ALWAYS_TRUE_METHOD;
        static {
            try {
                ALWAYS_TRUE_METHOD = Guard.AlwaysTrue.class.getMethod("alwaysTrue");
            }
            catch ( NoSuchMethodException e ) {
                throw new ExceptionInInitializerError(e);
            }
        }

        public AlwaysTrueResolver(@NotNull Class<? extends Annotation> guardType) {
            super(guardType);
        }

        @NotNull
        @Override
        TestMethod findTestMethod(@NotNull GuardInstance instance) {
            return new TestMethod(TypeToken.of(instance.getTarget().getGenericValueType()),
                    ALWAYS_TRUE_METHOD,
                    Indy.alwaysTrueHandle(instance.getTarget().getValueType()));
        }
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

}
