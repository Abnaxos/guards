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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import ch.raffael.guards.GuardsInternalError;
import ch.raffael.guards.definition.Guard;

import static org.objectweb.asm.Opcodes.*;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class GuardsClassLoader extends ClassLoader {

    static final String GEN_PACKAGE = GuardsClassLoader.class.getPackage().getName().replace('.', '/') + "/$gen/";

    private final AtomicInteger invokerCounter = new AtomicInteger();
    private final String description;

    GuardsClassLoader(String description, ClassLoader parent) {
        super(parent);
        this.description = description;
    }

    @Override
    public String toString() {
        return GuardsClassLoader.class.getName() + "{" + description + "}";
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T implementAnnotation(Class<T> annotationClass, Map<String, Object> values) {
        // FIXME: do this using ASM
        ImmutableMap.Builder<String, Object> valuesBuilder = ImmutableMap.builder();
        valuesBuilder.putAll(values);
        for ( java.lang.reflect.Method m : annotationClass.getDeclaredMethods() ) {
            if ( !values.containsKey(m.getName()) ) {
                valuesBuilder.put(m.getName(), m.getDefaultValue());
            }
        }
        valuesBuilder.put("annotytionType", annotationClass);
        return (T)Proxy.newProxyInstance(this, new Class[] { annotationClass }, new AnnotationImplInvokcationHandler(annotationClass, valuesBuilder.build()));
    }

    @SuppressWarnings("unchecked")
    public Class<? extends CheckerBridge.Invoker> invokerClass(Class<? extends Guard.Checker> checkerClass, CheckerBridge.CheckerMethod checkerMethod) {
        Type targetType = Type.getObjectType(GEN_PACKAGE + "Invoker_" + invokerCounter.getAndIncrement());
        Type checkerType = Type.getType(checkerClass);
        Type valueType = Type.getType(checkerMethod.type);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        writer.visit(V1_7, ACC_PUBLIC + ACC_SUPER, targetType.getInternalName(), null, "ch/raffael/guards/agent/CheckerBridge$Invoker", null);
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
        writer.visitEnd();
        byte[] bytecode = writer.toByteArray();
        return (Class<? extends CheckerBridge.Invoker>)defineClass(targetType.getClassName(), bytecode, 0, bytecode.length);
    }

    private GeneratorAdapter method(ClassVisitor cv, int access, String name, String desc) {
        return new GeneratorAdapter(cv.visitMethod(access, name, desc, null, null), access, name, desc);
    }

    private static class AnnotationImplInvokcationHandler implements InvocationHandler {
        private final Map<String, Object> values;
        private AnnotationImplInvokcationHandler(Class<? extends Annotation> annotationClass, Map<String, Object> values) {
            this.values = ImmutableMap.<String, Object>builder()
                    .putAll(values)
                    .put("annotationType", annotationClass)
                    .build();
        }
        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
            Object value = values.get(method.getName());
            if ( value == null ) {
                throw new GuardsInternalError(values.get("annotationType") + ": No value for " + method.getName());
            }
            return value;
        }
    }

}
