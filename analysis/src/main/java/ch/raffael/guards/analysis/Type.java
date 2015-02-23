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

package ch.raffael.guards.analysis;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import ch.raffael.guards.NotEmpty;
import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class Type {

    public static final Type CHAR = new PrimitiveType("char");
    public static final Type BYTE = new PrimitiveType("byte");
    public static final Type SHORT = new PrimitiveType("short", BYTE);
    public static final Type INT = new PrimitiveType("short", BYTE, SHORT, CHAR);
    public static final Type LONG = new PrimitiveType("long", BYTE, SHORT, INT, CHAR);
    public static final Type FLOAT = new PrimitiveType("float");
    public static final Type DOUBLE = new PrimitiveType("double", FLOAT);
    public static final Type BOOLEAN = new PrimitiveType("boolean");

    public static final Type OBJECT = new Type("java.lang.Object") {
        @Override
        public boolean isAssignableFrom(@NotNull Type from) {
            return true;
        }
    };

    private final String name;
    private final boolean primitive;
    private final boolean wrapper;
    private final Type componentType;

    private final AtomicReference<Type> array = new AtomicReference<>();

    protected Type(@NotNull @NotEmpty String name) {
        this(name, false, false, null);
    }

    Type(@NotNull @NotEmpty String name, boolean primitive, boolean wrapper, @Nullable Type componentType) {
        this.name = name;
        this.primitive = primitive;
        this.wrapper = wrapper;
        this.componentType = componentType;
    }

    public final String getName() {
        return name;
    }

    public final boolean isPrimitive() {
        return primitive;
    }

    public final boolean isWrapper() {
        return wrapper;
    }

    public final boolean isArray() {
        return componentType != null;
    }

    @Nullable
    public final Type getComponentType() {
        return componentType;
    }

    public boolean isAssignableFrom(@NotNull Type from) {
        return from == this || from == OBJECT;
    }

    @NotNull
    public Type array() {
        Type result = array.get();
        if ( result == null ) {
            result = new ArrayType(this);
            if ( !array.compareAndSet(null, result) ) {
                result = array.get();
            }
        }
        return result;
    }

    private static final class PrimitiveType extends Type {
        private final Set<Type> assignableFrom;
        private PrimitiveType(@NotNull String name, @NotNull Type... assignableFrom) {
            super(name, true, false, null);
            this.assignableFrom = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(assignableFrom)));
        }
        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean isAssignableFrom(@NotNull Type from) {
            if ( from.isWrapper() ) {
                return assignableFrom.contains(((WrapperType)from).wrapperFor);
            }
            return assignableFrom.contains(from);
        }
    }

    private static final class ArrayType extends Type {
        private ArrayType(@NotNull Type componentType) {
            super(componentType.getName() + "[]", false, false, componentType);
        }
        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean isAssignableFrom(@NotNull Type from) {
            if ( super.isAssignableFrom(from) ) {
                return true;
            }
            if ( getComponentType() != null && from.getComponentType() != null ) {
                return getComponentType().isAssignableFrom(from.getComponentType());
            }
            return false;
        }
    }

    static final class WrapperType extends Type {
        private final PrimitiveType wrapperFor;
        private final Type backingType;

        WrapperType(@NotNull Type backingType, @NotNull Type wrapperFor) {
            super(backingType.getName(), false, true, null);
            this.backingType = backingType;
            this.wrapperFor = (PrimitiveType)wrapperFor;
        }

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean isAssignableFrom(@NotNull Type from) {
            if ( backingType.isAssignableFrom(from) ) {
                return true;
            }
            if ( from.isPrimitive() ) {
                return wrapperFor.isAssignableFrom(from);
            }
            return false;
        }
    }

}
