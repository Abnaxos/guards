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

import ch.raffael.guards.GuardsInternalError;
import ch.raffael.guards.Sensitive;
import ch.raffael.guards.agent.guava.reflect.TypeToken;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class GuardTarget {

    private final GuardableMember member;
    private final int parameterIndex;
    private final String parameterName;
    private final boolean sensitive;
    private final Class<?> valueType;
    private final TypeToken<?> genericValueType;

    GuardTarget(GuardableMember member, int parameterIndex, String parameterName) {
        this.member = member;
        this.parameterIndex = parameterIndex;
        if ( parameterName == null || parameterName.isEmpty() ) {
            if ( parameterIndex > 0 ) {
                this.parameterName = "arg" + parameterIndex;
            }
            else {
                this.parameterName = "return";
            }
        }
        else {
            this.parameterName = parameterName;
        }

        if ( parameterIndex < 0 ) {
            sensitive = member.getAnnotation(Sensitive.class) != null;
            valueType = member.getReturnType();
            genericValueType = TypeToken.of(member.getGenericReturnType());
        }
        else {
            if ( parameterIndex >= member.getParameterTypes().length ) {
                throw new GuardsInternalError("Parameter index out of bounds: " + parameterIndex + ">=" + member.getParameterTypes().length);
            }
            boolean sensitive = false;
            Annotation[][] allParameterAnnotations = member.getParameterAnnotations();
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
            this.valueType = member.getParameterTypes()[parameterIndex];
            this.genericValueType = TypeToken.of(member.getGenericParameterTypes()[parameterIndex]);
        }
    }

    public GuardableMember getMember() {
        return member;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public String getParameterName() {
        return parameterName;
    }

    boolean isSensitive() {
        return sensitive;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public TypeToken<?> getGenericValueType() {
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
        return parameterIndex == that.parameterIndex && member.equals(that.member);
    }

    @Override
    public int hashCode() {
        int result = member.hashCode();
        result = 31 * result + parameterIndex;
        return result;
    }

    public String toShortString() {
        return appendShortString(new StringBuilder()).toString();
    }

    public StringBuilder appendShortString(StringBuilder buf) {
        return buf.append(member.getDeclaringClass().getSimpleName()).append(".")
                .append(member.getName()).append('(').append(parameterName).append(')');
    }

    public StringBuilder appendFullString(StringBuilder buf) {
        return buf.append(member).append(":").append(parameterName).append('[').append(parameterIndex).append(']');
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
