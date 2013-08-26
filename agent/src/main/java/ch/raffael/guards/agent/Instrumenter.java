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

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import ch.raffael.guards.definition.Guard;

import static com.google.common.base.Objects.*;
import static org.objectweb.asm.Opcodes.*;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Instrumenter extends ClassVisitor {

    private static final Map<Type, Method> PRIMITIVE_CHECKS = ImmutableMap.<Type, Method>builder()
            .put(Type.INT_TYPE, Types.M_CHECK_INT)
            .put(Type.BYTE_TYPE, Types.M_CHECK_BYTE)
            .put(Type.SHORT_TYPE, Types.M_CHECK_SHORT)
            .put(Type.LONG_TYPE, Types.M_CHECK_LONG)
            .put(Type.FLOAT_TYPE, Types.M_CHECK_FLOAT)
            .put(Type.DOUBLE_TYPE, Types.M_CHECK_DOUBLE)
            .put(Type.CHAR_TYPE, Types.M_CHECK_CHAR)
            .put(Type.BOOLEAN_TYPE, Types.M_CHECK_BOOLEAN)
            .build();

    private final Type outermostType;
    private final GuardsTransformer.Mode mode;
    private final CheckerStore checkerStore;
    private final ClassScanner scanner;
    private Type type;

    Instrumenter(ClassVisitor cv, Type outermostType, GuardsTransformer.Mode mode, CheckerStore checkerStore, ClassScanner scanner) {
        super(ASM4, cv);
        this.outermostType = outermostType;
        this.mode = mode;
        this.checkerStore = checkerStore;
        this.scanner = scanner;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        type = Type.getObjectType(name);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        ClassScanner.MethodInfo info = scanner.getMethods().get(new Method(name, desc));
        return new MethodInstrumenter(super.visitMethod(access, name, desc, signature, exceptions), info);
    }

    private class MethodInstrumenter extends AdviceAdapter {
        private final ClassScanner.MethodInfo methodInfo;
        private MethodInstrumenter(MethodVisitor mv, ClassScanner.MethodInfo methodInfo) {
            super(ASM4, mv, methodInfo.access, methodInfo.method.getName(), methodInfo.method.getDescriptor());
            this.methodInfo = methodInfo;
        }

        @Override
        protected void onMethodEnter() {
            //if ( methodInfo.firstLine != null ) {
            //    Label lineLabel = newLabel();
            //    visitLabel(lineLabel);
            //    visitLineNumber(methodInfo.firstLine, lineLabel);
            //}
            boolean hasGuards = false;
            for ( ClassScanner.ParameterInfo param : methodInfo.parameters ) {
                if ( !param.guards.isEmpty() ) {
                    hasGuards = true;
                    break;
                }
            }
            if ( !hasGuards ) {
                return;
            }
            Label endLabel = genAssertCheck();
            for ( int i = 0; i < methodInfo.parameters.length; i++ ) {
                ClassScanner.ParameterInfo param = methodInfo.parameters[i];
                getStatic(outermostType, Types.F_CHECKER_STORE, Types.T_CHECKER_STORE);
                //S: checkerStore
                for ( ClassScanner.GuardDeclaration guard : param.guards ) {
                    CheckerBridge bridge =
                            new CheckerBridge(type, methodInfo.method,
                                              "Parameter " + param.name, param.type,
                                              Guard.Type.PARAMETER,
                                              ClassSynthesizer.get(guard.handle.getAnnotationClass()).implementAnnotation(guard.handle.getAnnotationClass(), guard.values),
                                              guard.handle,
                                              checkerStore.loader());
                    int guardIndex = checkerStore.add(bridge);
                    dup(); // duplicate the checker store, we'll re-use it
                    //S: checkerStore, checkerStore
                    push(guardIndex);
                    //S: checkerStore, checkerStore, guardIndex
                    invokeVirtual(Types.T_CHECKER_STORE, Types.M_CHECKER_STORE_GET);
                    //S: checkerStore, guardBridge
                    dup();
                    //S: checkerStore, guardBridge, guardBridge
                    Label onNull = newLabel();
                    ifNull(onNull);
                    //S: checkerStore, guardBridge
                    loadArg(i);
                    //S: checkerStore, guardBridge, arg
                    invokeVirtual(Types.T_CHECKER_BRIDGE, firstNonNull(PRIMITIVE_CHECKS.get(param.type), Types.M_CHECK_OBJECT));
                    //S: checkerStore, msg
                    genViolation(Guard.Type.PARAMETER);
                    Label cont = newLabel();
                    goTo(cont);
                    visitLabel(onNull);
                    //S: checkerStore, guardBridge
                    pop(); // pop the null guardBridge
                    //S: checkerStore
                    visitLabel(cont);
                    //S: checkerStore
                }
                pop(); // drop the CheckerStore
                //S: ---
            }
            if ( endLabel != null ) {
                visitLabel(endLabel);
            }
            //super.onMethodEnter();
        }

        @Override
        protected void onMethodExit(int opcode) {
            //super.onMethodExit(opcode);
            if ( opcode == ATHROW || opcode == RETURN ) {
                return;
            }
            if ( methodInfo.guards.isEmpty() ) {
                return;
            }
            Label endLabel = genAssertCheck();
            //S: RET
            for ( ClassScanner.GuardDeclaration guard : methodInfo.guards ) {
                CheckerBridge bridge =
                        new CheckerBridge(type, methodInfo.method,
                                          "Return value",
                                          methodInfo.method.getReturnType(),
                                          Guard.Type.RESULT,
                                          ClassSynthesizer.get(guard.handle.getAnnotationClass()).implementAnnotation(guard.handle.getAnnotationClass(), guard.values),
                                          guard.handle,
                                          checkerStore.loader());
                int guardIndex = checkerStore.add(bridge);
                if ( isDoubleWord(methodInfo.method.getReturnType()) ) {
                    dup2();
                }
                else {
                    dup();
                }
                //S: RET, RET
                getStatic(outermostType, Types.F_CHECKER_STORE, Types.T_CHECKER_STORE);
                //S: RET, RET, checkerStore
                push(guardIndex);
                //S: RET, RET, checkerStore, index
                invokeVirtual(Types.T_CHECKER_STORE, Types.M_CHECKER_STORE_GET);
                //S: RET, RET, bridge
                dup();
                //S: RET, RET, bridge, bridge
                Label onNull = newLabel();
                ifNull(onNull);
                //S: RET, RET, bridge
                if ( isDoubleWord(methodInfo.method.getReturnType()) ) {
                    // well, that's more complicated because you cannot use swap with long
                    // RET == W1, W2
                    // W1, W2, bridge
                    dupX2();
                    // bridge, W1, W2, bridge
                    pop();
                    // bridge, W1, W2
                    // => RET, bridge, RET
                }
                else {
                    swap();
                }
                //S: RET, bridge, RET
                invokeVirtual(Types.T_CHECKER_BRIDGE, firstNonNull(PRIMITIVE_CHECKS.get(methodInfo.method.getReturnType()), Types.M_CHECK_OBJECT));
                //S: RET, msg
                genViolation(Guard.Type.RESULT);
                Label cont = newLabel();
                goTo(cont);
                visitLabel(onNull);
                //S: RET, RET, bridge
                pop(); // pop the null bridge
                //S: RET, RET
                if ( isDoubleWord(methodInfo.method.getReturnType()) ) {
                    pop2();
                }
                else {
                    pop();
                }
                // S: RET
                visitLabel(cont);
                //S: RET
            }
            if ( endLabel != null ) {
                visitLabel(endLabel);
            }
        }

        private boolean isDoubleWord(Type type) {
            return type.equals(Type.LONG_TYPE) || type.equals(Type.DOUBLE_TYPE);
        }

        private void genViolation(Guard.Type guardType) {
            // right now, we've got on the stack: checkerStore, msg
            // if msg is != null, throw an exception, otherwise, the check succeeded
            dup();
            //S: msg, msg
            Label checkLabel = newLabel();
            ifNull(checkLabel);
            //S: msg
            // throwing the exception
            Type exceptionType;
            Method exceptionCtor = Types.M_CTOR_W_STRING;
            if ( mode == GuardsTransformer.Mode.ASSERT ) {
                exceptionType = Types.T_ASSERTION_ERROR;
                exceptionCtor = Types.M_ASSERTION_ERROR_CTOR;
            }
            else if ( guardType == Guard.Type.PARAMETER ) {
                exceptionType = Types.T_ILLEGAL_ARGUMENT_EXCEPTION;
            }
            else { // guardType == Guard.Type.RESULT
                exceptionType = Types.T_ILLEGAL_STATE_EXCEPTION;
            }
            newInstance(exceptionType);
            //S: msg, exc
            dupX1();
            //S: exc, msg, exc
            swap();
            //S: exc, exc, msg
            invokeConstructor(exceptionType, exceptionCtor);
            //S: exc
            throwException();
            // end of throw
            visitLabel(checkLabel);
            // drop the null message
            pop();
        }

        private Label genAssertCheck() {
            if ( mode == GuardsTransformer.Mode.ASSERT ) {
                getStatic(outermostType, Types.F_ASSERTIONS_ENABLED, Type.BOOLEAN_TYPE);
                Label endLabel = newLabel();
                visitJumpInsn(IFEQ, endLabel);
                return endLabel;
            }
            else {
                return null;
            }
        }

        // we're using COMPUTE_MAXS for now
        //@Override
        //public void visitMaxs(int maxStack, int maxLocals) {
        //    // we need up to 4 things on the stack, so, in the worst case, the max stack
        //    // size is 4 more than in the original method; we're just adding 4.
        //    // In ASSERT mode, it's up to 5
        //    super.visitMaxs(maxStack + (mode == GuardsAgent.Mode.ASSERT ? 5 : 4), maxLocals);
        //}
    }

}
