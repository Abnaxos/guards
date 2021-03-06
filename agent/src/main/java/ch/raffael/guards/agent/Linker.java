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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.raffael.guards.Min;
import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.guava.base.Optional;
import ch.raffael.guards.agent.guava.collect.Lists;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class Linker {

    private static final Linker NULL_LINKER = new Linker(Collections.<MethodPointer, MethodGuards>emptyMap());

    private final Map<MethodPointer, MethodGuards> callSiteCache;

    private Linker(@NotNull Map<MethodPointer, MethodGuards> callSiteCache) {
        this.callSiteCache = callSiteCache;
    }

    @NotNull
    static Linker create(@NotNull Class<?> type) {
        Map<MethodPointer, MethodGuards> methodGuards = new HashMap<>();
        for( Method method : type.getDeclaredMethods() ) {
            MethodGuards guards = guardsForMember(GuardableMember.of(method));
            if ( guards != null ) {
                methodGuards.put(new MethodPointer(method), guards);
            }
        }
        for( Constructor constructor : type.getDeclaredConstructors() ) {
            MethodGuards guards = guardsForMember(GuardableMember.of(constructor));
            if ( guards != null ) {
                methodGuards.put(new MethodPointer(constructor), guards);
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
    private static MethodGuards guardsForMember(@NotNull GuardableMember member) {
        if ( Modifier.isAbstract(member.getMember().getModifiers()) ) {
            return null;
        }
        boolean hasGuards = hasGuards(member.getAnnotations()) || hasGuards(member.getParameterAnnotations());
        if ( hasGuards ) {
            return new MethodGuards(member);
        }
        else {
            return null;
        }
    }

    private static boolean hasGuards(@NotNull Annotation[][] parameterAnnotations) {
        for( Annotation[] annotations : parameterAnnotations ) {
            if ( hasGuards(annotations) ) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasGuards(@NotNull Annotation[] annotations) {
        for( Annotation annotation : annotations ) {
            if ( isGuard(annotation) ) {
                return true;
            }
        }
        return false;
    }

    private static boolean isGuard(@NotNull Annotation annotation) {
        return GuardDefinition.get(annotation.annotationType()) != null;
    }

    @NotNull
    CallSite bootstrap(@NotNull MethodHandles.Lookup caller,
                       @NotNull MethodType type,
                       @NotNull String targetMethodName,
                       @NotNull String targetMethodDescriptor,
                       @NotNull int parameterIndex,
                       @NotNull String parameterName) {
        //assert type.returnType() == void.class;
        //assert type.parameterCount() == 1;
        //assert parameterIndex >= -1;
        MethodType targetType = MethodType.fromMethodDescriptorString(targetMethodDescriptor, caller.lookupClass().getClassLoader());
        MethodGuards guards = callSiteCache.get(new MethodPointer(targetMethodName, targetType));
        if ( guards == null ) {
            return new ConstantCallSite(Indy.nopHandle(type.parameterType(0)));
            //throw new GuardsInternalError("Guards requested for non-guarded method " + Diagnostics.toString(caller.lookupClass(), name, guardedMethodType));
        }
        else {
            return guards.getCallSite(parameterIndex, parameterName);
        }
    }

    private static final class MethodGuards {

        private final Options options = GuardsAgent.getInstance().getOptions();

        private final GuardableMember guardable;
        private final CallSiteHolder[] callSites;
        private MethodGuards(GuardableMember guardable) {
            this.guardable = guardable;
            callSites = new CallSiteHolder[guardable.getParameterTypes().length + 1];
            for( int i = 0; i < callSites.length; i++ ) {
                callSites[i] = new CallSiteHolder();
            }
        }
        private CallSite getCallSite(@Min(-1) int parameterIndex, String parameterName) {
            CallSiteHolder holder = callSites[parameterIndex + 1];
            if ( holder.callSite == null ) {
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
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
            Annotation[] annotations;
            GuardTarget target = new GuardTarget(guardable, parameterIndex, parameterName);
            if ( parameterIndex < 0 ) {
                annotations = guardable.getAnnotations();
            }
            else {
                Annotation[][] allAnnotations = guardable.getParameterAnnotations();
                if ( parameterIndex >= allAnnotations.length ) {
                    return new ConstantCallSite(Indy.nopHandle(target.getValueType()));
                }
                annotations = allAnnotations[parameterIndex];
            }
            if ( annotations.length == 0 ) {
                return new ConstantCallSite(Indy.nopHandle(target.getValueType()));
            }
            ArrayList<GuardInstance> guardInstances = new ArrayList<>(annotations.length);
            for( Annotation annotation : annotations ) {
                if ( isGuard(annotation) ) {
                    guardInstances.add(new GuardInstance(target, annotation, null));
                }
            }
            MethodHandle handle = null;
            for( GuardInstance guardInstance : Lists.reverse(guardInstances)) {
                handle = GuardDefinition.get(guardInstance.getAnnotation().annotationType()).resolveTestMethod(guardInstance, handle);
            }
            if ( handle == null ) {
                handle = Indy.nopHandle(target.getValueType());
            }
            if ( options.isXMutableCallSites() ) {
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
        private MethodPointer(Constructor constructor) {
            this("<init>", MethodType.methodType(void.class, constructor.getParameterTypes()));
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
