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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import ch.raffael.guards.GuardsInternalError;
import ch.raffael.guards.Repeal;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Types {

    static final Map<Class<?>, Primitive> PRIMITIVES_BY_CLASS;
    static final Map<Type, Primitive> PRIMITIVES_BY_TYPE;
    static final Map<Class<?>, Primitive> PRIMITIVES_BY_WRAPPER_CLASS;
    static final Map<Type, Primitive> PRIMITIVES_BY_WRAPPER_TYPE;
    static {
        Primitive p, i;
        Primitive[] primitives = {
                p = new Primitive(long.class, Long.class, null),
                i = new Primitive(int.class, Integer.class, p),
                p = new Primitive(short.class, Short.class, i),
                new Primitive(byte.class, Byte.class, p),
                p = new Primitive(double.class, Double.class, null),
                new Primitive(float.class, Float.class, p),
                new Primitive(char.class, Character.class, i),
                new Primitive(boolean.class, Boolean.class, null)
        };
        PRIMITIVES_BY_CLASS = primitiveMap(
                primitives, new Function<Primitive, Class<?>>() {
            @Override
            public Class<?> apply(Types.Primitive input) {
                return input.primitiveClass;
            }
        });
        PRIMITIVES_BY_TYPE = primitiveMap(
                primitives, new Function<Primitive, Type>() {
            @Override
            public Type apply(Types.Primitive input) {
                return input.primitiveType;
            }
        });
        PRIMITIVES_BY_WRAPPER_CLASS = primitiveMap(
                primitives, new Function<Primitive, Class<?>>() {
            @Override
            public Class<?> apply(Types.Primitive input) {
                return input.wrapperClass;
            }
        });
        PRIMITIVES_BY_WRAPPER_TYPE = primitiveMap(
                primitives, new Function<Primitive, Type>() {
            @Override
            public Type apply(Types.Primitive input) {
                return input.wrapperType;
            }
        });
    }
    private static <T> Map<T, Primitive> primitiveMap(Primitive[] primitives, Function<Primitive, T> keyFunction) {
        ImmutableMap.Builder<T, Primitive> builder = ImmutableMap.builder();
        for ( Primitive p : primitives ) {
            builder.put(keyFunction.apply(p), p);
        }
        return builder.build();
    }

    static final Type T_OBJECT = Type.getType(Object.class);
    static final Type T_OBJECT_ARRAY = Type.getType(Object[].class);

    static final Type T_CLASS = Type.getType(Class.class);
    static final Type T_METHOD = Type.getType(java.lang.reflect.Method.class);
    static final Method M_GET_CLASS_LOADER = getMethod(Class.class, "getClassLoader");
    static final Method M_ASSERTION_STATUS = getMethod(Class.class, "desiredAssertionStatus");
    static final Method M_GET_METHOD = getMethod(Class.class, "getMethod", String.class, Class[].class);
    static final Method M_METHOD_DEFAULT_VALUE = getMethod(java.lang.reflect.Method.class, "getDefaultValue");

    static final Type T_CHECKER_STORE = Type.getType(CheckerStore.class);
    static final Method M_CHECKER_STORE_GET = getMethod(CheckerStore.class, "get", int.class);
    static final Method M_CHECKER_STORE_RETRIEVE = getMethod(CheckerStore.class, "retrieve", ClassLoader.class, String.class);

    static final Type T_CHECKER_BRIDGE = Type.getType(CheckerBridge.class);
    static final Method M_CHECK_INT = getMethod(CheckerBridge.class, "check", int.class);
    static final Method M_CHECK_BYTE = getMethod(CheckerBridge.class, "check", byte.class);
    static final Method M_CHECK_SHORT = getMethod(CheckerBridge.class, "check", short.class);
    static final Method M_CHECK_LONG = getMethod(CheckerBridge.class, "check", long.class);
    static final Method M_CHECK_FLOAT = getMethod(CheckerBridge.class, "check", float.class);
    static final Method M_CHECK_DOUBLE = getMethod(CheckerBridge.class, "check", double.class);
    static final Method M_CHECK_CHAR = getMethod(CheckerBridge.class, "check", char.class);
    static final Method M_CHECK_BOOLEAN = getMethod(CheckerBridge.class, "check", boolean.class);
    static final Method M_CHECK_OBJECT = getMethod(CheckerBridge.class, "check", Object.class);
    static final Type T_INVOKER = Type.getType(CheckerBridge.Invoker.class);

    static final Type T_GUARDS_INTERNAL_ERROR = Type.getType(GuardsInternalError.class);

    static final Type T_ILLEGAL_STATE_EXCEPTION = Type.getType(IllegalStateException.class);
    static final Type T_ILLEGAL_ARGUMENT_EXCEPTION = Type.getType(IllegalArgumentException.class);
    static final Type T_ASSERTION_ERROR = Type.getType(AssertionError.class);
    static final Method M_ASSERTION_ERROR_CTOR = new Method("<init>", "(Ljava/lang/Object;)V");

    static final Method M_CTOR = new Method("<init>", "()V");
    static final Method M_CTOR_W_STRING = new Method("<init>", "(Ljava/lang/String;)V");
    static final Method M_TO_STRING = new Method("toString", "()Ljava/lang/String;");

    static final Type T_MAP = Type.getType(Map.class);
    static final Method M_MAP_REMOVE = getMethod(Map.class, "remove", Object.class);
    static final Type T_HASHMAP = Type.getType(HashMap.class);
    static final Method M_HASHMAP_CTOR_COPY = getConstructor(HashMap.class, Map.class);

    static final Type T_ARRAYS = Type.getType(Arrays.class);

    static final String F_CHECKER_STORE = "$$ch$raffael$guards$checkerStore";
    static final String F_ASSERTIONS_ENABLED = "$$ch$raffael$guards$assertionsEnabled";

    static final Type T_REPEAL = Type.getType(Repeal.class);

    static boolean isDoubleWord(Type type) {
        return type.equals(Type.LONG_TYPE) || type.equals(Type.DOUBLE_TYPE);
    }

    static Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        try {
            return Method.getMethod(clazz.getDeclaredMethod(name, args));
        }
        catch ( NoSuchMethodException e ) {
            throw new ExceptionInInitializerError("Cannot find Method for " + clazz + ": " + name + ": " + Arrays.asList(args));
        }
    }

    static Method getConstructor(Class<?> clazz, Class<?>... args) {
        try {
            return Method.getMethod(clazz.getDeclaredConstructor(args));
        }
        catch ( NoSuchMethodException e ) {
            throw new ExceptionInInitializerError("Cannot find Constructor for " + clazz + ": " + Arrays.asList(args));
        }
    }

    static class Primitive {
        final Class<?> primitiveClass;
        final Class<?> wrapperClass;
        final Type primitiveType;
        final Type wrapperType;
        final Primitive next;
        private Primitive(Class<?> primitiveClass, Class<?> wrapperClass, Primitive next) {
            this.primitiveClass = primitiveClass;
            this.wrapperClass = wrapperClass;
            this.next = next;
            primitiveType = Type.getType(primitiveClass);
            wrapperType = Type.getType(wrapperClass);
        }
    }

}
