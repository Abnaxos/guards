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
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.raffael.guards.IntRange;
import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.guava.base.Optional;
import ch.raffael.guards.agent.guava.collect.Lists;
import ch.raffael.guards.definition.Guard;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class Linker {

    private static final Linker NULL_LINKER = new Linker(Collections.<MethodPointer, MethodGuards>emptyMap());

    private final Map<MethodPointer, MethodGuards> callSiteCache;

    private Linker(Map<MethodPointer, MethodGuards> callSiteCache) {
        this.callSiteCache = callSiteCache;
    }

    @NotNull
    static Linker create(@NotNull Class<?> type) {
        Map<MethodPointer, MethodGuards> methodGuards = new HashMap<>();
        for( Method method : type.getDeclaredMethods() ) {
            MethodGuards guards = guardsForMethod(method);
            if ( guards != null ) {
                methodGuards.put(new MethodPointer(method), guards);
            }
        }
        if ( methodGuards.isEmpty() ) {
            return NULL_LINKER;
        }
        else {
            return new Linker(Collections.unmodifiableMap(methodGuards));
        }
    }

    @Nullable
    private static MethodGuards guardsForMethod(@NotNull Method method) {
        if ( Modifier.isAbstract(method.getModifiers()) ) {
            return null;
        }
        boolean hasGuards = hasGuards(method) || hasGuards(method.getParameterAnnotations());
        if ( hasGuards ) {
            return new MethodGuards(method);
        }
        else {
            return null;
        }
    }

    private static boolean hasGuards(Annotation[][] parameterAnnotations) {
        for( Annotation[] annotations : parameterAnnotations ) {
            for( Annotation annotation : annotations ) {
                if ( isGuard(annotation) ) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasGuards(AnnotatedElement element) {
        for( Annotation annotation : element.getAnnotations() ) {
            if ( isGuard(annotation) ) {
                return true;
            }
        }
        return false;
    }

    private static boolean isGuard(Annotation annotation) {
        return annotation.annotationType().getAnnotation(Guard.class) != null;
    }

    CallSite bootstrap(MethodHandles.Lookup caller, MethodType type, String targetMethodName, String targetMethodDescriptor, int parameterIndex, String parameterName) {
        //assert type.returnType() == void.class;
        //assert type.parameterCount() == 1;
        //assert parameterIndex >= -1;
        MethodType targetType = MethodType.fromMethodDescriptorString(targetMethodDescriptor, caller.lookupClass().getClassLoader());
        MethodGuards guards = callSiteCache.get(new MethodPointer(targetMethodName, targetType));
        if ( guards == null ) {
            return Indy.resolveToNop(type.parameterType(0));
            //throw new GuardsInternalError("Guards requested for non-guarded method " + Diagnostics.toString(caller.lookupClass(), name, guardedMethodType));
        }
        else {
            return guards.getCallSite(parameterIndex, parameterName);
        }
    }

    private static final class MethodGuards {

        private final Options options = GuardsAgent.getInstance().getOptions();

        private final Method method;
        private final CallSiteHolder[] callSites;
        private MethodGuards(Method method) {
            this.method = method;
            callSites = new CallSiteHolder[method.getParameterTypes().length + 1];
            for( int i = 0; i < callSites.length; i++ ) {
                callSites[i] = new CallSiteHolder();
            }
        }
        private CallSite getCallSite(@IntRange(min = -1) int parameterIndex, String parameterName) {
            CallSiteHolder holder = callSites[parameterIndex + 1];
            if ( holder.callSite == null ) {
                synchronized ( holder ) {
                    if ( holder.callSite == null ) {
                        CallSite callSite = createCallSite(parameterIndex, parameterName);
                        holder.callSite = Optional.fromNullable(callSite);
                        return callSite;
                    }
                    else {
                        return holder.callSite.orNull();
                    }
                }
            }
            else {
                return holder.callSite.orNull();
            }
        }

        private CallSite createCallSite(int parameterIndex, String parameterName) {
            Class<?> type;
            Annotation[] annotations;
            if ( parameterIndex < 0 ) {
                type = method.getReturnType();
                annotations = method.getAnnotations();
            }
            else {
                type = method.getParameterTypes()[parameterIndex];
                Annotation[][] allAnnotations = method.getParameterAnnotations();
                if ( parameterIndex >= allAnnotations.length ) {
                    return Indy.resolveToNop(type);
                }
                annotations = allAnnotations[parameterIndex];
            }
            if ( annotations.length == 0 ) {
                return Indy.resolveToNop(type);
            }
            ArrayList<GuardInstance> guardInstances = new ArrayList<>(annotations.length);
            for( Annotation annotation : annotations ) {
                if ( isGuard(annotation) ) {
                    guardInstances.add(new GuardInstance(new GuardTarget(method, parameterIndex, parameterName), annotation));
                }
            }
            MethodHandle handle;
            if ( guardInstances.isEmpty() ) {
                return Indy.resolveToNop(type);
            }
            if ( options.getInvocationMethod() == Options.InvocationMethod.INVOKER) {
                handle = TestInvokers.invoker(guardInstances).asType(MethodType.methodType(void.class, type));
            }
            else {
                handle = null;
                for( GuardInstance guardInstance : Lists.reverse(guardInstances)) {
                    handle = Indy.appendGuardMethod(handle, guardInstance);
                }
                assert handle != null;
            }
            if ( options.isMutableCallSites() ) {
                return new MutableCallSite(handle);
            }
            else {
                return new ConstantCallSite(handle);
            }
        }

    }

    private static final class MethodPointer {
        private final String name;
        private final MethodType methodType;
        private MethodPointer(String name, MethodType methodType) {
            this.name = name;
            this.methodType = methodType;
        }
        private MethodPointer(Method method) {
            this(method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
        }
        @Override
        public boolean equals(Object o) {
            if ( this == o ) {
                return true;
            }
            if ( o == null || getClass() != o.getClass() ) {
                return false;
            }
            MethodPointer that = (MethodPointer)o;
            return methodType.equals(that.methodType) && name.equals(that.name);
        }
        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + methodType.hashCode();
            return result;
        }
        @Override
        public String toString() {
            return name + methodType;
        }
    }

    private static final class CallSiteHolder {
        private volatile Optional<CallSite> callSite;
    }


}
