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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.MapMaker;
import org.objectweb.asm.Type;

import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.Index;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ObjectEquality")
final class GuardHandle {

    private static final GuardHandle NOT_A_GUARD = new GuardHandle(new Guard() {
        @SuppressWarnings("ZeroLengthArrayAllocation")
        private final Type[] types = new Type[0];
        @Override
        public String message() {
            return "";
        }
        @Override
        public Type[] types() {
            return types;
        }
        @Override
        public Class<? extends Annotation> annotationType() {
            return Guard.class;
        }
    }, null, "", new Method[0]);
    private static final CheckerHandle INVALID_CHECKER = new CheckerHandle(null, null);
    private static ConcurrentMap<Class, GuardHandle> STORE = new MapMaker().weakKeys().makeMap();

    private final Class<? extends Annotation> annotationClass; // FIXME: memory leak here!
    private final Type annotationType;
    private final Guard guardAnnotation;
    private final Set<Guard.Type> types;
    private final AtomicReference<CheckerHandle> checkerHandle = new AtomicReference<>(null);
    private final String annotationMsgFormat;
    private final Method[] violationArgGetters;

    private GuardHandle(Guard guardAnnotation, Class<? extends Annotation> annotationClass, String annotationMsgFormat, Method[] violationArgGetters) {
        this.guardAnnotation = guardAnnotation;
        this.annotationClass = annotationClass;
        this.annotationMsgFormat = annotationMsgFormat;
        this.violationArgGetters = violationArgGetters;
        if ( guardAnnotation.types().length == 0 ) {
            types = Collections.emptySet();
        }
        else {
            types = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(guardAnnotation.types())));
        }
        if ( annotationClass != null ) {
            annotationType = Type.getType(annotationClass);
        }
        else {
            annotationType = null;
        }
    }

    @Override
    public String toString() {
        if ( annotationClass == null ) {
            return "NOT_A_GUARD";
        }
        else {
            return "Guard{" + annotationClass.getSimpleName() + "}";
        }
    }

    static GuardHandle get(ClassLoader loader, String annotationBinaryName) {
        Class annotationClass;
        try {
            annotationClass = Class.forName(annotationBinaryName, false, loader);
        }
        catch ( ClassNotFoundException e ) {
            return null;
        }
        GuardHandle metaData = STORE.get(annotationClass);
        if ( metaData == null ) {
            metaData = loadGuard(annotationClass);
            GuardHandle prev = STORE.putIfAbsent(annotationClass, metaData);
            if ( prev != null ) {
                metaData = prev;
            }
        }
        return metaData != NOT_A_GUARD ? metaData : null;
    }

    Set<Guard.Type> getTypes() {
        return types;
    }

    @SuppressWarnings("unchecked")
    private static GuardHandle loadGuard(Class annotationClass) {
        if ( !annotationClass.isAnnotation() ) {
            if ( Log.traceEnabled() ) {
                Log.trace("%s is not a guard: Not an annotation", annotationClass);
            }
            return NOT_A_GUARD;
        }
        Guard guard = (Guard)annotationClass.getAnnotation(Guard.class);
        if ( guard == null ) {
            if ( Log.traceEnabled() ) {
                Log.trace("%s is not a guard: Not annotated as @Guard", annotationClass);
            }
            return NOT_A_GUARD;
        }
        String format = guard.message();
        ArrayList<Method> argMethods = new ArrayList<>();
        for ( Method m : annotationClass.getMethods() ) {
            Index index = m.getAnnotation(Index.class);
            if ( index != null ) {
                if ( index.value() < 0 ) {
                    format = "[Inconsistent @Index] " + format;
                    argMethods.clear();
                    break;
                }
                while ( index.value() >= argMethods.size() ) {
                    argMethods.add(null);
                }
                argMethods.set(index.value(), m);
            }
        }
        for ( int i = 0; i < argMethods.size(); i++ ) {
            if ( argMethods.get(i) == null ) {
                format = "[Inconsistent @Index] " + format;
                argMethods.clear();
                break;
            }
        }
        return new GuardHandle(guard, annotationClass,
                               format,
                               argMethods.isEmpty() ? null : argMethods.toArray(new Method[argMethods.size()]));
    }

    Type getAnnotationType() {
        return annotationType;
    }

    Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    Guard.Checker newChecker(Annotation guardAnnotation, Guard.Type type, Class<?> valueClass) {
        if ( checkerHandle.get() == null ) {
            Class<?> checkerClass;
            Constructor<?> constructor = null;
            try {
                checkerClass = Class.forName(annotationClass.getName() + "$Checker", false, annotationClass.getClassLoader());
                if ( !Guard.Checker.class.isAssignableFrom(checkerClass) ) {
                    Log.error("Checker %s does not extend Guard.Checker", checkerClass);
                }
                else {
                    if ( !Modifier.isPublic(checkerClass.getModifiers()) ) {
                        checkerClass = null;
                        Log.error("Checker %s is not public", checkerClass);
                    }
                    else if ( Modifier.isAbstract(checkerClass.getModifiers()) ) {
                        checkerClass = null;
                        Log.error("Checker %s is abstract", checkerClass);
                    }
                    if ( checkerClass != null ) {
                        try {
                            constructor = checkerClass.getConstructor(annotationClass, Guard.Type.class, Class.class);
                        }
                        catch ( NoSuchMethodException e ) {
                            try {
                                constructor = checkerClass.getConstructor(Annotation.class, Guard.Type.class, Class.class);
                            }
                            catch ( NoSuchMethodException e1 ) {
                                Log.error("No suitable constructor found for checker %s", checkerClass);
                            }
                        }
                        if ( constructor != null ) {
                            if ( !Modifier.isPublic(constructor.getModifiers()) ) {
                                constructor = null;
                                Log.error("Constructor %s is not public", constructor);
                            }
                        }
                    }
                }
                if ( checkerClass != null && constructor != null ) {
                    checkerHandle.compareAndSet(null, new CheckerHandle(checkerClass, constructor));
                }
                else {
                    checkerHandle.compareAndSet(null, INVALID_CHECKER);
                }
            }
            catch ( ClassNotFoundException e ) {
                if ( Log.debugEnabled() ) {
                    Log.debug("No checker class found for %s", annotationClass.getName());
                }
                checkerHandle.compareAndSet(null, INVALID_CHECKER);
            }
        }
        CheckerHandle handle = checkerHandle.get();
        assert handle != null;
        if ( handle.constructor != null ) {
            try {
                return handle.constructor.newInstance(guardAnnotation, type, valueClass);
            }
            catch ( InstantiationException | IllegalAccessException | InvocationTargetException e ) {
                Log.error("Error invoking %s", e, handle.constructor);
            }
        }
        return null;
    }

    String violationMessage(Annotation annotation) {
        if ( violationArgGetters == null ) {
            return guardAnnotation.message();
        }
        else {
            try {
                Object[] args = new Object[violationArgGetters.length];
                for ( int i = 0; i < args.length; i++ ) {
                    args[i] = violationArgGetters[i].invoke(annotation);
                }
                return String.format(guardAnnotation.message(), args);
            }
            catch ( Exception e ) {
                return "[" + e + "] " + guardAnnotation.message();
            }
        }
    }

    private static final class CheckerHandle {
        final Class<? extends Guard.Checker> checker;
        final Constructor<? extends Guard.Checker> constructor;
        @SuppressWarnings("unchecked")
        private CheckerHandle(Class<?> checker, Constructor<?> constructor) {
            this.checker = (Class<? extends Guard.Checker>)checker;
            this.constructor = (Constructor<? extends Guard.Checker>)constructor;
        }
    }

}
