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
import java.util.Arrays;
import java.util.List;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.guava.collect.ImmutableList;
import ch.raffael.guards.agent.guava.reflect.TypeToken;
import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.Guard.Handler;
import ch.raffael.guards.definition.GuardFlag;
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
    private final Constructor<? extends Handler<?>> constructor;
    private final List<TestMethod> testMethods;

    @SuppressWarnings("unchecked")
    Resolver(@NotNull Class<? extends Annotation> guardType, @NotNull Class<? extends Handler<?>> handlerType) {
        this.guardType = guardType;
        testNulls = Arrays.asList(guardType.getAnnotation(Guard.class).flags()).contains(GuardFlag.TEST_NULLS);
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
    }

    /**
     * Constructor for AlwaysTrue
     */
    private Resolver(Class<? extends Annotation> guardType) {
        this.guardType = guardType;
        this.testNulls = false;
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
            if ( candidate.getParameterCount() == 0 ) {
                if ( constructor == null ) {
                    constructor = candidate;
                }
            }
            else if ( candidate.getParameterCount() == 1) {
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
            if ( constructor.getParameterCount() == 0 ) {
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
        TypeToken<?> valueType = TypeToken.of(target.getGenericValueType());
        TestMethod testMethod = null;
        for( TestMethod candidate : testMethods ) {
            if ( candidate.valueType.isAssignableFrom(valueType) ) {
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
                        throw new IllegalGuardError(target + ": Ambiguous test method: Both " + testMethod.method + " and " + candidate.method + " match");
                    }
                }
            }
        }
        if ( testMethod == null ) {
            throw new IllegalGuardError(target + ": No matching test method found for " + guardType.getName());
        }
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

}
