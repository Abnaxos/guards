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

import ch.raffael.guards.NotNull;
import ch.raffael.guards.agent.guava.reflect.Reflection;
import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.runtime.ContractViolationError;
import ch.raffael.guards.runtime.GuardsInternalError;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class GuardInstance {

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

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable e) throws T {
        throw (T)e;
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
        appendMessage(buf);
        if ( target.isSensitive() ) {
            buf.append("\n  (value concealed for security)");
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
        for( int i = 0; i < stackTrace.length; i++ ) {
            if ( Reflection.getPackageName(stackTrace[i].getClassName()).equals(MY_PACKAGE_NAME) ) {
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

    private void appendMessage(StringBuilder buf) {
        // TODO: error message substitution
        String message = guardType.configuration.message().trim();
        if ( message.isEmpty() ) {
            message = guardType.annotationType.getName();
        }
        buf.append(message);
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
