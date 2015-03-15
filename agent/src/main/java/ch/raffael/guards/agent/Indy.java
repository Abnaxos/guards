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

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import ch.raffael.guards.GuardNotApplicableError;
import ch.raffael.guards.IllegalGuardError;
import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.asm.Handle;
import ch.raffael.guards.agent.asm.Opcodes;
import ch.raffael.guards.agent.asm.Type;
import ch.raffael.guards.agent.asm.commons.Method;

import static ch.raffael.guards.agent.asm.Type.getType;
import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodType.methodType;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Indy {

    static final Handle BOOTSTRAP_ASM_HANDLE = new Handle(
            Opcodes.H_INVOKESTATIC, getType(Indy.class).getInternalName(), "bootstrap",
            new Method("boostrap",
                    getType(CallSite.class),
                    new Type[] {
                            getType(MethodHandles.Lookup.class),
                            getType(String.class),
                            getType(MethodType.class),
                            getType(String.class),
                            getType(String.class),
                            Type.INT_TYPE,
                            getType(String.class)
                    }).getDescriptor());

    //private static final MethodHandle NOP_HANDLE =
    //        MethodHandles.constant(Void.class, null).asType(methodType(void.class));
    private static final MethodHandle DEDICATED_NOP_HANDLE;
    private static final MethodHandle TEST_NOT_NULL;
    private static final MethodHandle ILLEGAL_GUARD_HANDLE;
    private static final MethodHandle GUARD_NOT_APPLICABLE_HANDLE;
    static final MethodHandle GUARD_VIOLATION_HANDLE;
    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            DEDICATED_NOP_HANDLE = lookup.findStatic(Indy.class, "nop", methodType(void.class));
            TEST_NOT_NULL = lookup.findStatic(Indy.class, "testNotNull", methodType(boolean.class, Object.class));
            ILLEGAL_GUARD_HANDLE = lookup.findStatic(Indy.class, "illegalGuard",
                    methodType(void.class, String.class));
            GUARD_NOT_APPLICABLE_HANDLE = lookup.findStatic(Indy.class, "guardNotApplicable",
                    methodType(void.class, String.class));
            GUARD_VIOLATION_HANDLE = lookup.findVirtual(GuardInstance.class, "guardViolation", methodType(void.class, Object.class));
        }
        catch ( NoSuchMethodException | IllegalAccessException e ) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final static ClassValue<Linker> LINKERS = new ClassValue<Linker>() {
        @Override
        protected Linker computeValue(Class<?> type) {
            return Linker.create(type);
        }
    };

    private Indy() {
    }

    private static void nop() {
    }

    private static boolean testNotNull(Object value) {
        return value != null;
    }

    private static void illegalGuard(String message) {
        throw new IllegalGuardError(message);
    }

    private static void guardNotApplicable(String message) {
        throw new GuardNotApplicableError(message);
    }

    static ConstantCallSite resolveToNop(Class<?> type) {
        return new ConstantCallSite(nopHandle(type));
    }

    static MethodHandle alwaysTrueHandle(Class<?> type) {
        MethodHandle methodHandle = constant(boolean.class, true);
        if ( type != null ) {
            methodHandle = dropArguments(methodHandle, 0, type);
        }
        return methodHandle;
    }

    static MethodHandle testNotNullHandle(Class<?> type) {
        return TEST_NOT_NULL.asType(methodType(boolean.class, type));
    }

    @NotNull
    static MethodHandle nopHandle(@Nullable Class<?> type) {
        MethodHandle nop = null;
        switch ( GuardsAgent.getInstance().getOptions().getXNopMethod() ) {
            case MH_CONSTANT:
                nop = constant(Void.class, null).asType(methodType(void.class));
                break;
            case DEDICATED_METHOD:
                nop = DEDICATED_NOP_HANDLE;
                break;
        }
        assert nop != null;
        if ( type != null ) {
            nop = dropArguments(nop, 0, type);
        }
        return nop;
    }

    static ConstantCallSite resolveToIllegalGuard(String message, Class<?> type) {
        return new ConstantCallSite(dropArguments(
                ILLEGAL_GUARD_HANDLE.bindTo(message), 0, type));
    }

    static ConstantCallSite resolveToGuardNotApplicable(String message, Class<?> type) {
        return new ConstantCallSite(dropArguments(
                GUARD_NOT_APPLICABLE_HANDLE.bindTo(message), 0, type));
    }

    @NotNull
    static MethodHandle prependGuardMethod(@NotNull MethodHandle guardMethod, @NotNull GuardInstance instance, @Nullable MethodHandle prependTo) {
        Class<?> type = instance.getTarget().getValueType();
        return MethodHandles.guardWithTest(
                guardMethod.asType(methodType(boolean.class, type)),
                prependTo == null ? nopHandle(type) : prependTo,
                GUARD_VIOLATION_HANDLE.bindTo(instance).asType(methodType(void.class, type)));
    }

    public static CallSite bootstrap(MethodHandles.Lookup caller, String ignoredName, MethodType type, String targetMethodName, String targetMethodDescriptor, int parameterIndex, String parameterName) {
        assert type.returnType() == void.class;
        assert type.parameterCount() == 1;
        if ( GuardsAgent.getInstance().getOptions().isXNopMode() ) {
            return new ConstantCallSite(nopHandle(type.parameterType(0)));
        }
        else {
            return LINKERS.get(caller.lookupClass()).bootstrap(caller, type, targetMethodName, targetMethodDescriptor, parameterIndex, parameterName);
        }
    }

}
