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

import ch.raffael.guards.agent.asm.AnnotationVisitor;
import ch.raffael.guards.agent.asm.ClassVisitor;
import ch.raffael.guards.agent.asm.MethodVisitor;
import ch.raffael.guards.agent.asm.Type;
import ch.raffael.guards.agent.asm.commons.AdviceAdapter;

import static ch.raffael.guards.agent.IntFlags.containsFlag;
import static ch.raffael.guards.agent.asm.Opcodes.ACC_ANNOTATION;
import static ch.raffael.guards.agent.asm.Opcodes.ACC_STATIC;
import static ch.raffael.guards.agent.asm.Opcodes.ASM5;
import static ch.raffael.guards.agent.asm.Opcodes.V1_7;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Instrumenter extends ClassVisitor {

    private final Options options;
    private final ClassLoader loader;

    Instrumenter(Options options, ClassLoader loader, ClassVisitor cv) {
        super(ASM5, cv);
        this.loader = loader;
        this.options = options;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if ( version < V1_7 ) {
            // TODO: can we just upgrade the bytecode version and if so, from what versions?
            throw new CancelException("Bytecode version <1.7 (" + V1_7 + "): " + version);
        }
        if ( containsFlag(access, ACC_ANNOTATION) ) {
            throw new CancelException("Is an annotation type");
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, String[] exceptions) {
        final Type[] argumentTypes = Type.getArgumentTypes(desc);
        final Type returnType = Type.getReturnType(desc);
        final boolean isStatic = containsFlag(access, ACC_STATIC);
        return new AdviceAdapter(ASM5, super.visitMethod(access, name, desc, signature, exceptions), access, name, desc) {
            boolean hasMethodAnnotations = false;
            boolean[] hasParameterAnnotations = new boolean[argumentTypes.length];

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if ( visible ) {
                    hasMethodAnnotations = true;
                }
                return super.visitAnnotation(desc, visible);
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
                if ( visible ) {
                    hasParameterAnnotations[parameter] = true;
                }
                return super.visitParameterAnnotation(parameter, desc, visible);
            }

            @Override
            public void visitCode() {
                super.visitCode();
                checkParameters();
            }

            @Override
            protected void onMethodEnter() {
                // DO NOT INSERT PARAMETER CHECK CODE HERE
                //
                // The reason is that this one is *too* correct. ;)
                // If the instrumented method is a constructor, this method gets called only after
                // the super constructor has been called. While it's usually the way to go to
                // instrument constructors, in our case, we'd like to do this earlier.
                //
                // The bytecode of checkParameters() won't change any of the object's state, what
                // it does, is perfectly OK to do before calling the super constructor -- we're just
                // messing with some parameters. The bytecode verifier doesn't complain neither.
                // We're *actually* failing fast that way. ;)
                //
                //checkParameters();
                super.onMethodEnter();
            }

            @Override
            protected void onMethodExit(int opcode) {
                checkReturnValue(opcode);
                super.onMethodExit(opcode);
            }

            private void checkParameters() {
                if ( argumentTypes.length == 0 ) {
                    return;
                }
                for( int i = 0; i < argumentTypes.length; i++ ) {
                    if ( options.isInstrumentAll() || hasParameterAnnotations[i] ) {
                        loadArg(i);
                        invokeDynamic("guard:arg" + i, "(" + argumentTypes[i].getDescriptor() + ")V", Indy.BOOTSTRAP_ASM_HANDLE, name, desc, i);
                    }
                }
            }

            private void checkReturnValue(int opcode) {
                if ( options.isInstrumentAll() || hasMethodAnnotations ) {
                    String guardDesc = "(" + Type.getReturnType(desc) + ")V";
                    String indyName = "guard:return";
                    switch ( opcode ) {
                        case IRETURN: // return int
                            assert returnType.equals(Type.INT_TYPE) : "Return opcode mismatch: opcode=" + opcode + " / type=" + returnType;
                            dup();
                            invokeDynamic(indyName, guardDesc, Indy.BOOTSTRAP_ASM_HANDLE, name, desc, -1);
                            break;
                        case LRETURN: // return long
                            assert returnType.equals(Type.LONG_TYPE) : "Return opcode mismatch: opcode=" + opcode + " / type=" + returnType;
                            dup2();
                            invokeDynamic(indyName, guardDesc, Indy.BOOTSTRAP_ASM_HANDLE, name, desc, -1);
                            break;
                        case FRETURN: // return float
                            assert returnType.equals(Type.FLOAT_TYPE) : "Return opcode mismatch: opcode=" + opcode + " / type=" + returnType;
                            dup();
                            invokeDynamic(indyName, guardDesc, Indy.BOOTSTRAP_ASM_HANDLE, name, desc, -1);
                            break;
                        case DRETURN: // return double
                            assert returnType.equals(Type.DOUBLE_TYPE) : "Return opcode mismatch: opcode=" + opcode + " / type=" + returnType;
                            dup2();
                            invokeDynamic(indyName, guardDesc, Indy.BOOTSTRAP_ASM_HANDLE, name, desc, -1);
                            break;
                        case ARETURN: // return reference
                            dup();
                            invokeDynamic(indyName, guardDesc, Indy.BOOTSTRAP_ASM_HANDLE, name, desc, -1);
                        case RETURN: // return void => no instrumentation
                        case ATHROW: // throw exception => no instrumentation
                            break;
                        default:
                            assert false : "Unexpected onMethodExit() opcode: " + opcode;
                    }
                }
            }
        };
    }
}
