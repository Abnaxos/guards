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
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import ch.raffael.guards.Sensitive;
import ch.raffael.guards.runtime.GuardsInternalError;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class GuardTarget {

    private final Method method;
    private final int parameterIndex;
    private final boolean sensitive;
    private final Class<?> valueType;
    private final Type genericValueType;

    GuardTarget(Method method, int parameterIndex) {
        this.method = method;
        this.parameterIndex = parameterIndex;
        if ( parameterIndex < 0 ) {
            sensitive = method.getAnnotation(Sensitive.class) != null;
            valueType = method.getReturnType();
            genericValueType = method.getGenericReturnType();
        }
        else {
            if ( parameterIndex >= method.getParameterTypes().length ) {
                throw new GuardsInternalError("Parameter index out of bounds: " + parameterIndex + ">=" + method.getParameterTypes().length);
            }
            boolean sensitive = false;
            Annotation[][] allParameterAnnotations = method.getParameterAnnotations();
            if ( parameterIndex < allParameterAnnotations.length ) {
                Annotation[] targetAnnotations = allParameterAnnotations[parameterIndex];
                if ( targetAnnotations != null ) {
                    for( Annotation targetAnnotation : targetAnnotations ) {
                        if ( targetAnnotation.annotationType().equals(Sensitive.class) ) {
                            sensitive = true;
                            break;
                        }
                    }
                }
            }
            this.sensitive = sensitive;
            this.valueType = method.getParameterTypes()[parameterIndex];
            this.genericValueType = method.getGenericParameterTypes()[parameterIndex];
        }
    }

    public Method getMethod() {
        return method;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    boolean isSensitive() {
        return sensitive;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public Type getGenericValueType() {
        return genericValueType;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        GuardTarget that = (GuardTarget)o;
        return parameterIndex == that.parameterIndex && method.equals(that.method);
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + parameterIndex;
        return result;
    }

    public String toShortString() {
        return appendShortString(new StringBuilder()).toString();
    }

    public StringBuilder appendShortString(StringBuilder buf) {
        buf.append(method.getDeclaringClass().getSimpleName()).append(".").append(method.getName()).append("[");
        if ( parameterIndex < 0 ) {
            buf.append("return");
        }
        else {
            buf.append(ParameterNames.get(method, parameterIndex));
        }
        buf.append("]");
        return buf;
    }

    public StringBuilder appendFullString(StringBuilder buf) {
        buf.append(method);
        if ( parameterIndex < 0 ) {
            buf.append(":return");
        }
        else {
            buf.append(":").append(ParameterNames.get(method, parameterIndex));
        }
        return buf;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getClass().getSimpleName()).append("{");
        appendFullString(buf);
        buf.append("}");
        return buf.toString();
    }

}
