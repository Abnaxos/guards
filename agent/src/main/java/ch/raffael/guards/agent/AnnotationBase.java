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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.util.Arrays;
import java.util.Map;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class AnnotationBase implements Annotation, Serializable {

    static final Type TYPE = Type.getType(AnnotationBase.class);
    static final Method M_VALUE = Types.getMethod(AnnotationBase.class, "$$$value$$$", Map.class, String.class, Object.class);
    static final Method M_INIT = Types.getMethod(AnnotationBase.class, "$$$init$$$", Map.class);
    static final Method M_CTOR = Types.getConstructor(AnnotationBase.class, Class.class);

    private final Class<? extends Annotation> annotationType;
    private StringBuilder valuesStringBuilder = new StringBuilder();
    private volatile String stringValue = "?";

    protected AnnotationBase(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    final public String toString() {
        return stringValue;
    }

    @Override
    public final Class<? extends Annotation> annotationType() {
        return annotationType;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected final Object $$$value$$$(Map<String, Object> values, String name, Object defaultValue) {
        Object value = values.remove(name);
        if ( value == null ) {
            if ( defaultValue == null ) {
                throw new AnnotationFormatError("No value specified for " + annotationType + "::" + name);
            }
            value = defaultValue;
        }
        if ( valuesStringBuilder.length() != 0 ) {
            valuesStringBuilder.append(", ");
        }
        valuesStringBuilder.append(name).append('=');
        if ( value.getClass().isArray() ) {
            if ( value instanceof int[] ) {
                valuesStringBuilder.append(Arrays.toString((int[])value));
            }
            else if ( value instanceof byte[] ) {
                valuesStringBuilder.append(Arrays.toString((byte[])value));
            }
            else if ( value instanceof short[] ) {
                valuesStringBuilder.append(Arrays.toString((short[])value));
            }
            else if ( value instanceof long[] ) {
                valuesStringBuilder.append(Arrays.toString((long[])value));
            }
            else if ( value instanceof float[] ) {
                valuesStringBuilder.append(Arrays.toString((float[])value));
            }
            else if ( value instanceof double[] ) {
                valuesStringBuilder.append(Arrays.toString((double[])value));
            }
            else if ( value instanceof char[] ) {
                valuesStringBuilder.append(Arrays.toString((char[])value));
            }
            else if ( value instanceof boolean[] ) {
                valuesStringBuilder.append(Arrays.toString((boolean[])value));
            }
            else {
                valuesStringBuilder.append(Arrays.toString((Object[])value));
            }
        }
        else {
            valuesStringBuilder.append(value);
        }
        return value;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected final void $$$init$$$(Map<String, Object> values) {
        if ( !values.isEmpty() ) {
            throw new AnnotationFormatError("Unknown values specified: " + values);
        }
        stringValue = "@" + annotationType.getName() + "(" + valuesStringBuilder + ")";
        valuesStringBuilder = null;
    }

}
