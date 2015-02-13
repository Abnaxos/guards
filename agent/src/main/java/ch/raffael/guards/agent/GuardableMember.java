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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
abstract class GuardableMember {

    private GuardableMember() {
    }

    @NotNull
    static GuardableMember of(@NotNull Member member) {
        if ( member instanceof Method ) {
            return new GuardableMethod((Method)member);
        }
        else if ( member instanceof Constructor ) {
            return new GuardableConstructor((Constructor)member);
        }
        else {
            throw new IllegalArgumentException("Method or Constructor expected");
        }
    }

    @NotNull
    abstract Member getMember();

    String getName() {
        return getMember().getName();
    }

    Class<?> getDeclaringClass() {
        return getMember().getDeclaringClass();
    }

    @NotNull
    abstract Class<?>[] getParameterTypes();

    @NotNull
    abstract Type[] getGenericParameterTypes();

    @Nullable
    <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return ((AnnotatedElement)getMember()).getAnnotation(annotationType);
    }

    @NotNull
    Annotation[] getAnnotations() {
        return ((AnnotatedElement)getMember()).getAnnotations();
    }

    @NotNull
    abstract Annotation[][] getParameterAnnotations();

    abstract Class<?> getReturnType();

    abstract Type getGenericReturnType();

    @Override
    public int hashCode() {
        return getMember().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof GuardableMember) && getMember().equals(((GuardableMember)obj).getMember());
    }

    @Override
    public String toString() {
        return getMember().toString();
    }

    private static class GuardableMethod extends GuardableMember {
        private final Method method;
        private GuardableMethod(Method method) {
            this.method = method;
        }
        @NotNull
        @Override
        Method getMember() {
            return method;
        }
        @NotNull
        @Override
        public Class<?>[] getParameterTypes() {
            return method.getParameterTypes();
        }

        @NotNull
        @Override
        Type[] getGenericParameterTypes() {
            return method.getGenericParameterTypes();
        }

        @NotNull
        @Override
        Annotation[][] getParameterAnnotations() {
            return method.getParameterAnnotations();
        }

        @Override
        Class<?> getReturnType() {
            return method.getReturnType();
        }

        @Override
        Type getGenericReturnType() {
            return method.getGenericReturnType();
        }
    }

    private static class GuardableConstructor extends GuardableMember {
        private final Constructor constructor;
        private GuardableConstructor(Constructor constructor) {
            this.constructor = constructor;
        }
        @NotNull
        @Override
        Member getMember() {
            return constructor;
        }
        @NotNull
        @Override
        Class<?>[] getParameterTypes() {
            return constructor.getParameterTypes();
        }

        @NotNull
        @Override
        Type[] getGenericParameterTypes() {
            return constructor.getGenericParameterTypes();
        }

        @NotNull
        @Override
        Annotation[][] getParameterAnnotations() {
            return constructor.getParameterAnnotations();
        }

        @Override
        Class<?> getReturnType() {
            return void.class;
        }

        @Override
        Type getGenericReturnType() {
            return void.class;
        }
    }

}
