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
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Sensitive;
import ch.raffael.guards.agent.guava.collect.ForwardingMap;
import ch.raffael.guards.agent.guava.collect.ImmutableBiMap;
import ch.raffael.guards.agent.guava.collect.ImmutableMap;
import ch.raffael.guards.agent.guava.collect.ImmutableSet;
import ch.raffael.guards.agent.guava.reflect.Reflection;
import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.runtime.ContractViolationError;
import ch.raffael.guards.runtime.GuardsInternalError;
import ch.raffael.guards.runtime.internal.Substitutor;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class GuardInstance {

    private static final Set<String> NON_ANNOTATION_VALUES = ImmutableSet.of(
            "clone", "finalize", "getClass", "hashCode", "toString", "annotationType");

    private static final ClassValue<Type> TYPES = new ClassValue<Type>() {
        @SuppressWarnings("unchecked")
        @Override
        protected Type computeValue(Class<?> type) {
            assert type.isAnnotation();
            Guard configuration = type.getAnnotation(Guard.class);
            if ( configuration == null ) {
                throw new GuardsInternalError("Not a guard type: " + type);
            }
            return new Type((Class<? extends Annotation>)type, configuration);
        }
    };

    private final static String MY_PACKAGE_NAME = Reflection.getPackageName(GuardInstance.class);
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final GuardTarget target;
    private final Annotation annotation;
    private final Type guardType;
    private final Resolver.TestMethod testMethod;

    GuardInstance(GuardTarget target, Annotation annotation) {
        this.target = target;
        this.annotation = annotation;
        guardType = TYPES.get(annotation.annotationType());
        this.testMethod = Resolver.resolverFor(annotation.annotationType()).findTestMethod(this);
    }

    GuardTarget getTarget() {
        return target;
    }

    Annotation getAnnotation() {
        return annotation;
    }

    Type getGuardType() {
        return guardType;
    }

    @NotNull
    Method getTestMethod() {
        return testMethod.method();
    }

    @NotNull
    MethodHandle getTestMethodHandle() {
        return testMethod.methodHandle();
    }

    private boolean dummyTestMethod(Object obj) {
        //System.err.println("Guard Instance: " + target + " / " + annotation + " / value " + obj);
        return true;
        //return !annotation.annotationType().getName().equals("guards.Fail");
    }

    void guardViolation(Object value) {
        StringBuilder buf = new StringBuilder();
        buf.append("Contract Violation at ");
        target.appendShortString(buf);
        //buf.append(" for value ");
        //if ( target.isSensitive() ) {
        //    buf.append("(concealed)");
        //}
        //else if ( value == null ) {
        //    buf.append("null");
        //}
        //else {
        //    buf.append("'").append(value).append("'");
        //}
        buf.append(": ");
        appendMessage(buf, value);
        if ( target.isSensitive() ) {
            buf.append("\n  ").append(Sensitive.SENSITIVE_MSG);
        }
        else {
            buf.append("\n  Value : ");
            if ( value == null ) {
                buf.append("null");
            }
            else {
                buf.append("(").append(value.getClass().getName()).append(") ");
                buf.append(value);
            }
        }
        buf.append("\n  Guard : ").append(getAnnotation());
        buf.append("\n  Target: ");
        target.appendFullString(buf);
        buf.append("\n  Method: ").append(getTestMethod());
        ContractViolationError violationError = new ContractViolationError(buf.toString());
        StackTraceElement[] stackTrace = violationError.getStackTrace();
        int removeCount = 0;
        for( StackTraceElement aStackTrace : stackTrace ) {
            if ( Reflection.getPackageName(aStackTrace.getClassName()).equals(MY_PACKAGE_NAME) ) {
                removeCount++;
            }
            else {
                if ( getTarget().getParameterIndex() >= 0 && removeCount < stackTrace.length - 1 ) {
                    // Remove the called method. Logically, the calling method violated the,
                    // contract, even though technically, the called method did the check. Reflect
                    // that in the stack trace.
                    removeCount++;
                }
                break;
            }
        }
        if ( removeCount > 0 ) {
            StackTraceElement[] newStackTrace = new StackTraceElement[stackTrace.length - removeCount];
            System.arraycopy(stackTrace, removeCount, newStackTrace, 0, newStackTrace.length);
            violationError.setStackTrace(newStackTrace);
        }
        throw violationError;
    }

    private void appendMessage(StringBuilder buf, Object value) {
        // TODO: error message substitution
        String message = guardType.configuration.message().trim();
        if ( message.isEmpty() ) {
            buf.append(guardType.annotationType.getName());
        }
        else {
            substituteAnnotationValues(buf, message, value);
        }
    }

    private void substituteAnnotationValues(StringBuilder buf, String string, final Object value) {
        Substitutor.substitute(buf, string, new ForwardingMap<String, String>() {
            private Map<String, String> delegate;
            @Override
            protected Map<String, String> delegate() {
                if ( delegate == null ) {
                    ImmutableMap.Builder<String, String> builder = ImmutableBiMap.builder();
                    for( Method method : annotation.getClass().getMethods() ) {
                        if ( method.getParameterTypes().length == 0 && method.getReturnType() != void.class && !NON_ANNOTATION_VALUES.contains(method.getName()) ) {
                            Object value;
                            try {
                                value = method.invoke(annotation);
                            }
                            catch ( Exception e ) {
                                Logging.LOG.log(Level.SEVERE, "Error getting value " + method.getName() + " from " + annotation, e);
                                value = "ERROR:" + e.getClass().getName();
                            }
                            builder.put(method.getName(), String.valueOf(value));
                        }
                        if ( target.isSensitive() ) {
                            builder.put("return", Sensitive.SENSITIVE_MSG);
                        }
                        else {
                            builder.put("return", String.valueOf(value));
                        }
                    }
                    delegate = builder.build();
                }
                return delegate;
            }
        });
    }

    static final class Type {

        private final Class<? extends Annotation> annotationType;
        private final Guard configuration;

        private Type(Class<? extends Annotation> annotationType, Guard configuration) {
            this.annotationType = annotationType;
            this.configuration = configuration;
        }

        Class<? extends Annotation> annotationType() {
            return annotationType;
        }

        Guard configuration() {
            return configuration;
        }

    }

}
