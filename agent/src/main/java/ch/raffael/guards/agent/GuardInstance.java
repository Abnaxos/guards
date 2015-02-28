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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import ch.raffael.guards.Sensitive;
import ch.raffael.guards.agent.guava.base.Joiner;
import ch.raffael.guards.agent.guava.collect.ForwardingMap;
import ch.raffael.guards.agent.guava.collect.ImmutableBiMap;
import ch.raffael.guards.agent.guava.collect.ImmutableList;
import ch.raffael.guards.agent.guava.collect.ImmutableMap;
import ch.raffael.guards.agent.guava.collect.ImmutableSet;
import ch.raffael.guards.agent.guava.reflect.Reflection;
import ch.raffael.guards.definition.Message;
import ch.raffael.guards.runtime.ContractViolationError;
import ch.raffael.guards.runtime.internal.Substitutor;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class GuardInstance {

    private static final Set<String> NON_ANNOTATION_VALUES = ImmutableSet.of(
            "clone", "finalize", "getClass", "hashCode", "toString", "annotationType");

    private final static String MY_PACKAGE_NAME = Reflection.getPackageName(GuardInstance.class);

    private final GuardTarget target;
    private final List<Annotation> annotationPath;
    private final String message;
    private Method testMethod;

    GuardInstance(GuardTarget target, Annotation annotation, String message) {
        this.target = target;
        this.annotationPath = ImmutableList.of(annotation);
        if ( message == null ) {
            Message msgAnnotation = annotation.annotationType().getAnnotation(Message.class);
            if ( msgAnnotation != null ) {
                message = msgAnnotation.value();
            }
        }
        this.message = message;
    }

    GuardInstance(GuardInstance parent, Annotation annotation) {
        this.target = parent.getTarget();
        this.annotationPath = ImmutableList.<Annotation>builder().addAll(parent.annotationPath).add(annotation).build();
        if ( parent.message == null ) {
            Message msgAnnotation = annotation.annotationType().getAnnotation(Message.class);
            if ( msgAnnotation != null ) {
                message = msgAnnotation.value();
            }
            else {
                message = null;
            }
        }
        else {
            message = parent.message;
        }
    }

    void updateTestMethod(Method testMethod) {
        this.testMethod = testMethod;
    }

    GuardTarget getTarget() {
        return target;
    }

    Annotation getAnnotation() {
        return annotationPath.get(annotationPath.size() - 1);
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
        buf.append("\n  Value : ");
        if ( target.isSensitive() ) {
            buf.append(Sensitive.SENSITIVE_MSG);
        }
        else {
            if ( value == null ) {
                buf.append("null");
            }
            else {
                if ( target.getValueType().isPrimitive() ) {
                    buf.append("(").append(target.getValueType().getName()).append(")");
                }
                else {
                    buf.append("(").append(value.getClass().getName()).append(")");
                }
                buf.append(value);
            }
        }
        buf.append("\n  Guard : ");
        Joiner.on(" -> ").appendTo(buf, annotationPath);
        buf.append("\n  Target: ");
        target.appendFullString(buf);
        buf.append("\n  Method: ").append(testMethod != null ? testMethod : "(unknown)");
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
        if ( message != null ) {
            substituteAnnotationValues(buf, message, value);
        }
        else {
            buf.append(getAnnotation());
        }
    }

    private void substituteAnnotationValues(StringBuilder buf, String string, final Object value) {
        Substitutor.substitute(buf, string, new ForwardingMap<String, String>() {
            private Map<String, String> delegate;
            @Override
            protected Map<String, String> delegate() {
                if ( delegate == null ) {
                    ImmutableMap.Builder<String, String> builder = ImmutableBiMap.builder();
                    Annotation annotation = getAnnotation();
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
                    }
                    if ( target.isSensitive() ) {
                        builder.put("", Sensitive.SENSITIVE_MSG);
                    }
                    else {
                        builder.put("", String.valueOf(value));
                    }
                    builder.put("annotationType", "@" + annotation.annotationType().getName());
                    builder.put("this", annotation.toString());
                    delegate = builder.build();
                }
                return delegate;
            }
        });
    }

}
