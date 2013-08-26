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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import ch.raffael.guards.GuardsInternalError;
import ch.raffael.guards.definition.Guard;

import static org.objectweb.asm.Opcodes.*;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class ClassSynthesizer extends ClassLoader {

    private static final ClassSynthesizer SYSTEM_SYNTHESIZER = new ClassSynthesizer("SYSTEM", ClassLoader.getSystemClassLoader());
    private static final ConcurrentMap<Class<?>, ClassSynthesizer> SYNTHESIZERS = new MapMaker().weakKeys().makeMap();

    static final String SYNTHETIC_PACKAGE = ClassSynthesizer.class.getPackage().getName().replace('.', '/') + "/$synthetic/";

    private final AtomicInteger invokerCounter = new AtomicInteger();
    private final AtomicInteger annotationCounter = new AtomicInteger();
    private final String description;

    private final Map<Class<? extends Guard.Checker>, Map<Class<?>, Class<? extends CheckerBridge.Invoker>>> invokerClasses =
            new MapMaker().weakKeys().makeMap();
    private final Map<Class<? extends Annotation>, Class<? extends Annotation>> annotationImplClasses =
            new MapMaker().weakKeys().makeMap();

    private ClassSynthesizer(String description, ClassLoader parent) {
        super(parent);
        this.description = description;
    }

    public static ClassSynthesizer get(Class<?> clazz) {
        if ( GuardsTransformer.isSystemClassLoader(clazz.getClassLoader()) ) {
            return SYSTEM_SYNTHESIZER;
        }
        ClassSynthesizer synthesizer = SYNTHESIZERS.get(clazz);
        if ( synthesizer == null ) {
            synthesizer = new ClassSynthesizer(clazz.toString(), clazz.getClassLoader());
            ClassSynthesizer prev = SYNTHESIZERS.putIfAbsent(clazz, synthesizer);
            if ( prev != null ) {
                synthesizer = prev;
            }
        }
        return synthesizer;
    }

    @Override
    public String toString() {
        return ClassSynthesizer.class.getName() + "{" + description + "}";
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T implementAnnotation(Class<T> annotationClass, Map<String, Object> values) {
        synchronized ( annotationImplClasses ) {
            Class<? extends Annotation> implClass = annotationImplClasses.get(annotationClass);
            if ( implClass == null ) {
                implClass = (Class<? extends Annotation>)createAnnotationImplClass(annotationClass);
                annotationImplClasses.put(annotationClass, implClass);
            }
            try {
                return ((Constructor<T>)implClass.getDeclaredConstructor(Map.class)).newInstance(values);
            }
            catch ( InvocationTargetException e ) {
                Throwables.propagateIfInstanceOf(e.getTargetException(), Error.class);
                throw new GuardsInternalError("Exception creating annotation implementation", e.getTargetException());
            }
            catch ( Exception e ) {
                throw new GuardsInternalError("Exception creating annotation implementation", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Class<? extends CheckerBridge.Invoker> invokerClass(Class<? extends Guard.Checker> checkerClass, CheckerBridge.CheckerMethod checkerMethod) {
        synchronized ( invokerClasses ) {
            Map<Class<?>, Class<? extends CheckerBridge.Invoker>> forCheckerClass = invokerClasses.get(checkerClass);
            if ( forCheckerClass == null ) {
                forCheckerClass = new HashMap<>();
                invokerClasses.put(checkerClass, forCheckerClass);
            }
            Class<? extends CheckerBridge.Invoker> invokerClass = forCheckerClass.get(checkerMethod.type);
            if ( invokerClass == null ) {
                invokerClass = createInvokerClass(checkerClass, checkerMethod);
                forCheckerClass.put(checkerMethod.type, invokerClass);
            }
            return invokerClass;
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends CheckerBridge.Invoker> createInvokerClass(Class<? extends Guard.Checker> checkerClass, CheckerBridge.CheckerMethod checkerMethod) {
        Type targetType = Type.getObjectType(SYNTHETIC_PACKAGE + "Invoker_" + invokerCounter.getAndIncrement());
        Type checkerType = Type.getType(checkerClass);
        Type valueType = Type.getType(checkerMethod.type);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        writer.visit(V1_7, ACC_PUBLIC + ACC_SUPER + ACC_FINAL, targetType.getInternalName(), null, "ch/raffael/guards/agent/CheckerBridge$Invoker", null);
        FieldVisitor checkerField = writer.visitField(ACC_PRIVATE + ACC_FINAL, "checker", checkerType.getDescriptor(), null, null);
        checkerField.visitEnd();
        GeneratorAdapter ctor = method(writer, ACC_PUBLIC, "<init>", "(" + checkerType.getDescriptor() + ")V");
        ctor.visitCode();
        ctor.loadThis();
        ctor.invokeConstructor(Types.T_INVOKER, Types.M_CTOR);
        ctor.loadThis();
        ctor.loadArg(0);
        ctor.putField(targetType, "checker", checkerType);
        ctor.visitInsn(RETURN);
        ctor.visitMaxs(1, 2);
        ctor.visitEnd();
        GeneratorAdapter check;
        if ( checkerMethod.type.isPrimitive() ) {
            check = method(writer, ACC_PUBLIC, "check", "(" + valueType.getDescriptor() + ")Z");
        }
        else {
            check = method(writer, ACC_PUBLIC, "check", "(Ljava/lang/Object;)Z");
        }
        check.visitCode();
        check.loadThis();
        check.getField(targetType, "checker", checkerType);
        check.loadArg(0);
        if ( !checkerMethod.type.isPrimitive() ) {
            check.checkCast(valueType);
        }
        check.invokeVirtual(checkerType, new Method("check", Type.BOOLEAN_TYPE, new Type[] { valueType }));
        check.returnValue();
        check.visitMaxs(2, 2);
        check.visitEnd();
        GeneratorAdapter toString = method(writer, ACC_PUBLIC, "toString", "()Ljava/lang/String;");
        toString.push("Invoker{" + checkerClass.getEnclosingClass().getName() + ":" + checkerMethod.type + "}");
        toString.returnValue();
        toString.visitMaxs(1, 0);
        toString.visitEnd();
        writer.visitEnd();
        byte[] bytecode = writer.toByteArray();
        return (Class<? extends CheckerBridge.Invoker>)defineClass(targetType.getClassName(), bytecode, 0, bytecode.length);
    }

    private <T extends Annotation> Class<?> createAnnotationImplClass(Class<T> annotationClass) {
        Type targetType = Type.getObjectType(SYNTHETIC_PACKAGE + "Annotation_" + annotationCounter.getAndIncrement());
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        Type annotationType = Type.getType(annotationClass);
        writer.visit(V1_7, ACC_PUBLIC + ACC_SUPER + ACC_FINAL, targetType.getInternalName(), null,
                     AnnotationBase.TYPE.getInternalName(), new String[] { annotationType.getInternalName() });
        java.lang.reflect.Method[] methods = annotationClass.getDeclaredMethods();
        boolean hasDefaults = false;
        for ( java.lang.reflect.Method method : methods ) {
            Type type = Type.getType(method.getReturnType());
            FieldVisitor fv = writer.visitField(ACC_PRIVATE + ACC_FINAL, "$val$" + method.getName(), type.getDescriptor(), null, null);
            fv.visitEnd();
            if ( method.getDefaultValue() != null ) {
                hasDefaults = true;
                fv = writer.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "$def$" + method.getName(), Types.T_OBJECT.getDescriptor(), null, null);
                fv.visitEnd();
            }
        }
        if ( hasDefaults ) {
            GeneratorAdapter init = method(writer, ACC_STATIC, "<clinit>", "()V");
            init.visitCode();
            for ( java.lang.reflect.Method method : methods ) {
                if ( method.getDefaultValue() != null ) {
                    init.push(annotationType);
                    //S: targetType
                    init.push(method.getName());
                    //S: targetType, methodName
                    init.push(0);
                    //S: targetType, methodName, 0
                    init.newArray(Types.T_CLASS);
                    //S: targetType, methodName, Class[0]
                    init.invokeVirtual(Types.T_CLASS, Types.M_GET_METHOD);
                    //S: method
                    init.invokeVirtual(Types.T_METHOD, Types.M_METHOD_DEFAULT_VALUE);
                    //S: defaultValue
                    Label l = init.newLabel();
                    init.dup();
                    //S: defaultValue, defaultValue
                    init.ifNonNull(l);
                    init.throwException(Types.T_ILLEGAL_STATE_EXCEPTION, "Expected non-null default value for " + method.getName());
                    init.visitLabel(l);
                    //S: defaultValue
                    init.putStatic(targetType, "$def$" + method.getName(), Types.T_OBJECT);
                    //S:
                }
            }
            init.visitInsn(RETURN);
            init.visitMaxs(0, 0);
            init.visitEnd();
        }
        GeneratorAdapter ctor = method(writer, ACC_PUBLIC, "<init>", "(Ljava/util/Map;)V");
        ctor.visitCode();
        ctor.loadThis();
        //S: this
        ctor.push(annotationType);
        //S: this, annotationType
        ctor.invokeConstructor(AnnotationBase.TYPE, AnnotationBase.M_CTOR);
        //S:
        ctor.newInstance(Types.T_HASHMAP);
        //S: map
        ctor.dup();
        //S: map, map
        ctor.loadArg(0);
        //S: map, map, mapArg
        ctor.invokeConstructor(Types.T_HASHMAP, Types.M_HASHMAP_CTOR_COPY);
        //S: map
        for ( java.lang.reflect.Method method : methods ) {
            //S: map
            Type type = Type.getType(method.getReturnType());
            ctor.dup();
            //S: map, map
            ctor.loadThis();
            //S: map, map, this
            ctor.dupX1();
            //S: map, this, map, this
            ctor.swap();
            //S: map, this, this, map
            ctor.push(method.getName());
            //S: map, this, this, map, "fieldName"
            if ( method.getDefaultValue() != null ) {
                ctor.getStatic(targetType, "$def$" + method.getName(), Types.T_OBJECT);
            }
            else {
                ctor.visitInsn(ACONST_NULL);
            }
            //S: map, this, this, map, "fieldName", defaultValue
            ctor.invokeVirtual(AnnotationBase.TYPE, AnnotationBase.M_VALUE);
            //S: map, this, value
            ctor.unbox(type);
            ctor.putField(targetType, "$val$" + method.getName(), type);
            //S: map
        }
        //S: map
        ctor.loadThis();
        //S: map, this
        ctor.swap();
        //S: this, map
        ctor.invokeVirtual(AnnotationBase.TYPE, AnnotationBase.M_INIT);
        //S:
        ctor.visitInsn(RETURN);
        ctor.visitMaxs(0, 0); // will be calculated
        ctor.visitEnd();
        for ( java.lang.reflect.Method method : methods ) {
            Type type = Type.getType(method.getReturnType());
            GeneratorAdapter getter = method(writer, ACC_PUBLIC, method.getName(), "()" + type.getDescriptor());
            getter.visitCode();
            getter.loadThis();
            //S: this
            getter.getField(targetType, "$val$" + method.getName(), type);
            //S:  value
            if ( method.getReturnType().isArray() ) {
                // make a copy of the array
                getter.dup();
                //S: value, value
                getter.arrayLength();
                //S: value, length
                Type arrayType;
                if ( method.getReturnType().getComponentType().isPrimitive() ) {
                    arrayType = Type.getType(method.getReturnType());
                }
                else {
                    arrayType = Types.T_OBJECT_ARRAY;
                }
                getter.invokeStatic(Types.T_ARRAYS, new Method("copyOf", arrayType,
                                                               new Type[] { arrayType, Type.INT_TYPE }));
                //S: value (copy)
                getter.checkCast(Type.getType(method.getReturnType()));
            }
            getter.returnValue();
            getter.visitMaxs(0, 0);
            getter.visitEnd();
        }
        writer.visitEnd();
        byte[] bytecode = writer.toByteArray();
        return defineClass(targetType.getClassName(), bytecode, 0, bytecode.length);
    }

    private GeneratorAdapter method(ClassVisitor cv, int access, String name, String desc) {
        return new GeneratorAdapter(cv.visitMethod(access, name, desc, null, null), access, name, desc);
    }

}
