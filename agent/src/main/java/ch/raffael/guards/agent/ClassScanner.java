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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import ch.raffael.guards.definition.Guard;

import static org.objectweb.asm.Opcodes.*;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class ClassScanner extends ClassVisitor {

    private final Realm realm;

    private Realm.PackageInfo packageInfo;

    private final Map<Method, MethodInfo> methods = new HashMap<>();

    ClassScanner(Realm realm) {
        super(ASM4);
        this.realm = realm;
    }

    Map<Method, MethodInfo> getMethods() {
        return methods;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        int pos = name.lastIndexOf('/');
        if ( pos == -1 ) {
            packageInfo = realm.getPackageInfo("");
        }
        else {
            packageInfo = realm.getPackageInfo(name.substring(0, pos));
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        Method method = new Method(name, desc);
        MethodInfo info = new MethodInfo(access, method, realm);
        methods.put(method, info);
        return info;
    }

    static class MethodInfo extends MethodVisitor {
        private Realm realm;
        final int access;
        final Method method;
        final ParameterInfo[] parameters;
        final LinkedList<GuardDeclaration> guards = new LinkedList<>();
        Integer firstLine = null;
        MethodInfo(int access, Method method, Realm realm) {
            super(ASM4);
            this.access = access;
            this.method = method;
            this.realm = realm;
            Type[] argumentTypes = method.getArgumentTypes();
            parameters = new ParameterInfo[argumentTypes.length];
            for ( int i = 0; i < argumentTypes.length; i++ ) {
                parameters[i] = new ParameterInfo(i, argumentTypes[i]);
            }
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            super.visitLocalVariable(name, desc, signature, start, end, index);
            if ( name == null ) {
                // most certainly unnecessary, but let's stay on the safe side
                return;
            }
            if ( !Flags.containsAll(access, ACC_STATIC) ) {
                index--;
            }
            if ( index >= 0 && index < parameters.length ) {
                parameters[index].name = name;
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            super.visitAnnotation(desc, visible);
            GuardHandle guardHandle = realm.getGuardHandle(Type.getType(desc).getClassName());
            if ( guardHandle == null ) {
                return null;
            }
            else if ( !guardHandle.getTypes().contains(Guard.Type.RESULT) ) {
                Log.warning("Guard %s cannot be applied to method results, skipping", guardHandle);
                return null;
            }
            else {
                guards.add(new GuardDeclaration(realm, guardHandle));
                return guards.getLast();
            }
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            super.visitParameterAnnotation(parameter, desc, visible);
            GuardHandle guardHandle = realm.getGuardHandle(Type.getType(desc).getClassName());
            if ( guardHandle == null ) {
                return null;
            }
            else if ( !guardHandle.getTypes().contains(Guard.Type.PARAMETER) ) {
                Log.warning("Guard %s cannot be applied to parameters, skipping", guardHandle);
                return null;
            }
            else {
                parameters[parameter].guards.add(new GuardDeclaration(realm, guardHandle));
                return parameters[parameter].guards.getLast();
            }
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            super.visitLineNumber(line, start);
            if ( firstLine == null ) {
                firstLine = line;
            }
        }

        @Override
        public void visitEnd() {
            realm = null;
        }
    }

    static class ParameterInfo {
        final int index;
        final Type type;
        String name;
        final LinkedList<GuardDeclaration> guards = new LinkedList<>();
        ParameterInfo(int index, Type type) {
            this.index = index;
            this.type = type;
            name = "#" + index;
        }
    }

    static class GuardDeclaration extends AnnotationVisitor {
        private Realm realm;
        final GuardHandle handle;
        final Map<String, Object> values = new HashMap<>();
        GuardDeclaration(Realm realm, GuardHandle handle) {
            super(ASM4);
            this.realm = realm;
            this.handle = handle;
        }
        @Override
        public void visit(String name, Object value) {
            super.visit(name, value);
            values.put(name, value);
        }
        @SuppressWarnings("unchecked")
        @Override
        public void visitEnum(String name, String desc, String value) {
            super.visitEnum(name, desc, value);
            try {
                Class<?> enumClass = Class.forName(desc, false, realm.loader());
                // FIXME: make that lazy
                values.put(name, Enum.valueOf((Class<? extends Enum>)enumClass, value));
            }
            catch ( ClassNotFoundException e ) {
// FIXME: Handle exception
                e.printStackTrace();
            }
        }
        @Override
        public void visitEnd() {
            realm = null;
        }
    }

}
