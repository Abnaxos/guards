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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.util.List;

import ch.raffael.guards.NotEmpty;
import ch.raffael.guards.NotNull;
import ch.raffael.guards.runtime.GuardsInternalError;

import static java.lang.invoke.MethodType.methodType;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class TestInvokers {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private TestInvokers() {
    }

    static MethodHandle invoker(@NotNull @NotEmpty List<GuardInstance> guardInstances) {
        Class<?> type = guardInstances.get(0).getTarget().getValueType();
        if ( type == int.class ) {
            if ( guardInstances.size() == 1 ) {
                return IntInvoker.SINGLE.bindTo(new IntInvoker(guardInstances.get(0)));
            }
            else {
                IntInvoker[] invokers = new IntInvoker[guardInstances.size()];
                for( int i = 0; i < invokers.length; i++ ) {
                    invokers[i] = new IntInvoker(guardInstances.get(i));
                }
                return IntInvoker.MULTI.bindTo(invokers);
            }
        }
        else if ( type == long.class ) {
            if ( guardInstances.size() == 1 ) {
                return LongInvoker.SINGLE.bindTo(new LongInvoker(guardInstances.get(0)));
            }
            else {
                LongInvoker[] invokers = new LongInvoker[guardInstances.size()];
                for( int i = 0; i < invokers.length; i++ ) {
                    invokers[i] = new LongInvoker(guardInstances.get(i));
                }
                return LongInvoker.MULTI.bindTo(invokers);
            }
        }
        else if ( type == byte.class ) {
            if ( guardInstances.size() == 1 ) {
                return ByteInvoker.SINGLE.bindTo(new ByteInvoker(guardInstances.get(0)));
            }
            else {
                ByteInvoker[] invokers = new ByteInvoker[guardInstances.size()];
                for( int i = 0; i < invokers.length; i++ ) {
                    invokers[i] = new ByteInvoker(guardInstances.get(i));
                }
                return ByteInvoker.MULTI.bindTo(invokers);
            }
        }
        else if ( type == short.class ) {
            if ( guardInstances.size() == 1 ) {
                return ShortInvoker.SINGLE.bindTo(new ShortInvoker(guardInstances.get(0)));
            }
            else {
                ShortInvoker[] invokers = new ShortInvoker[guardInstances.size()];
                for( int i = 0; i < invokers.length; i++ ) {
                    invokers[i] = new ShortInvoker(guardInstances.get(i));
                }
                return ShortInvoker.MULTI.bindTo(invokers);
            }
        }
        else if ( type == float.class ) {
            if ( guardInstances.size() == 1 ) {
                return FloatInvoker.SINGLE.bindTo(new FloatInvoker(guardInstances.get(0)));
            }
            else {
                FloatInvoker[] invokers = new FloatInvoker[guardInstances.size()];
                for( int i = 0; i < invokers.length; i++ ) {
                    invokers[i] = new FloatInvoker(guardInstances.get(i));
                }
                return FloatInvoker.MULTI.bindTo(invokers);
            }
        }
        else if ( type == double.class ) {
            if ( guardInstances.size() == 1 ) {
                return DoubleInvoker.SINGLE.bindTo(new DoubleInvoker(guardInstances.get(0)));
            }
            else {
                DoubleInvoker[] invokers = new DoubleInvoker[guardInstances.size()];
                for( int i = 0; i < invokers.length; i++ ) {
                    invokers[i] = new DoubleInvoker(guardInstances.get(i));
                }
                return DoubleInvoker.MULTI.bindTo(invokers);
            }
        }
        else if ( type == boolean.class ) {
            if ( guardInstances.size() == 1 ) {
                return BooleanInvoker.SINGLE.bindTo(new BooleanInvoker(guardInstances.get(0)));
            }
            else {
                BooleanInvoker[] invokers = new BooleanInvoker[guardInstances.size()];
                for( int i = 0; i < invokers.length; i++ ) {
                    invokers[i] = new BooleanInvoker(guardInstances.get(i));
                }
                return BooleanInvoker.MULTI.bindTo(invokers);
            }
        }
        else if ( type == char.class ) {
            if ( guardInstances.size() == 1 ) {
                return CharInvoker.SINGLE.bindTo(new CharInvoker(guardInstances.get(0)));
            }
            else {
                CharInvoker[] invokers = new CharInvoker[guardInstances.size()];
                for( int i = 0; i < invokers.length; i++ ) {
                    invokers[i] = new CharInvoker(guardInstances.get(i));
                }
                return CharInvoker.MULTI.bindTo(invokers);
            }
        }
        else {
            if ( guardInstances.size() == 1 ) {
                return ObjectInvoker.SINGLE.bindTo(new ObjectInvoker(guardInstances.get(0)));
            }
            else {
                ObjectInvoker[] invokers = new ObjectInvoker[guardInstances.size()];
                for( int i = 0; i < invokers.length; i++ ) {
                    invokers[i] = new ObjectInvoker(guardInstances.get(i));
                }
                return ObjectInvoker.MULTI.bindTo(invokers);
            }
        }
    }

    private static final class IntInvoker {
        private static final MethodHandle SINGLE;
        private static final MethodHandle MULTI;
        static {
            try {
                SINGLE = LOOKUP.findVirtual(IntInvoker.class, "invoke", methodType(void.class, int.class));
                MULTI = LOOKUP.findStatic(IntInvoker.class, "invoke", methodType(void.class, IntInvoker[].class, int.class));
            }
            catch ( NoSuchMethodException | IllegalAccessException e ) {
                throw new GuardsInternalError(e);
            }
        }
        private final Proxy proxy;
        private final GuardInstance guardInstance;
        private IntInvoker(GuardInstance guardInstance) {
            this.guardInstance = guardInstance;
            proxy = MethodHandleProxies.asInterfaceInstance(Proxy.class, guardInstance.getTestMethodHandle());
        }
        void invoke(int value) {
            if ( !proxy.test(value) ) {
                guardInstance.guardViolation(value);
            }
        }
        static void invoke(IntInvoker[] invokers, int value) {
            for( IntInvoker invoker : invokers ) {
                invoker.invoke(value);
            }
        }
        public interface Proxy {
            boolean test(int value);
        }
    }

    private static final class LongInvoker {
        private static final MethodHandle SINGLE;
        private static final MethodHandle MULTI;
        static {
            try {
                SINGLE = LOOKUP.findVirtual(LongInvoker.class, "invoke", methodType(void.class, long.class));
                MULTI = LOOKUP.findStatic(LongInvoker.class, "invoke", methodType(void.class, LongInvoker[].class, long.class));
            }
            catch ( NoSuchMethodException | IllegalAccessException e ) {
                throw new GuardsInternalError(e);
            }
        }
        private final Proxy proxy;
        private final GuardInstance guardInstance;
        private LongInvoker(GuardInstance guardInstance) {
            this.guardInstance = guardInstance;
            proxy = MethodHandleProxies.asInterfaceInstance(Proxy.class, guardInstance.getTestMethodHandle());
        }
        void invoke(long value) {
            if ( !proxy.test(value) ) {
                guardInstance.guardViolation(value);
            }
        }
        static void invoke(LongInvoker[] invokers, long value) {
            for( LongInvoker invoker : invokers ) {
                invoker.invoke(value);
            }
        }
        public interface Proxy {
            boolean test(long value);
        }
    }

    private static final class ByteInvoker {
        private static final MethodHandle SINGLE;
        private static final MethodHandle MULTI;
        static {
            try {
                SINGLE = LOOKUP.findVirtual(ByteInvoker.class, "invoke", methodType(void.class, byte.class));
                MULTI = LOOKUP.findStatic(ByteInvoker.class, "invoke", methodType(void.class, ByteInvoker[].class, byte.class));
            }
            catch ( NoSuchMethodException | IllegalAccessException e ) {
                throw new GuardsInternalError(e);
            }
        }
        private final Proxy proxy;
        private final GuardInstance guardInstance;
        private ByteInvoker(GuardInstance guardInstance) {
            this.guardInstance = guardInstance;
            proxy = MethodHandleProxies.asInterfaceInstance(Proxy.class, guardInstance.getTestMethodHandle());
        }
        void invoke(byte value) {
            if ( !proxy.test(value) ) {
                guardInstance.guardViolation(value);
            }
        }
        static void invoke(ByteInvoker[] invokers, byte value) {
            for( ByteInvoker invoker : invokers ) {
                invoker.invoke(value);
            }
        }
        public interface Proxy {
            boolean test(byte value);
        }
    }

    private static final class ShortInvoker {
        private static final MethodHandle SINGLE;
        private static final MethodHandle MULTI;
        static {
            try {
                SINGLE = LOOKUP.findVirtual(ShortInvoker.class, "invoke", methodType(void.class, short.class));
                MULTI = LOOKUP.findStatic(ShortInvoker.class, "invoke", methodType(void.class, ShortInvoker[].class, short.class));
            }
            catch ( NoSuchMethodException | IllegalAccessException e ) {
                throw new GuardsInternalError(e);
            }
        }
        private final Proxy proxy;
        private final GuardInstance guardInstance;
        private ShortInvoker(GuardInstance guardInstance) {
            this.guardInstance = guardInstance;
            proxy = MethodHandleProxies.asInterfaceInstance(Proxy.class, guardInstance.getTestMethodHandle());
        }
        void invoke(short value) {
            if ( !proxy.test(value) ) {
                guardInstance.guardViolation(value);
            }
        }
        static void invoke(ShortInvoker[] invokers, short value) {
            for( ShortInvoker invoker : invokers ) {
                invoker.invoke(value);
            }
        }
        public interface Proxy {
            boolean test(short value);
        }
    }

    private static final class FloatInvoker {
        private static final MethodHandle SINGLE;
        private static final MethodHandle MULTI;
        static {
            try {
                SINGLE = LOOKUP.findVirtual(FloatInvoker.class, "invoke", methodType(void.class, float.class));
                MULTI = LOOKUP.findStatic(FloatInvoker.class, "invoke", methodType(void.class, FloatInvoker[].class, float.class));
            }
            catch ( NoSuchMethodException | IllegalAccessException e ) {
                throw new GuardsInternalError(e);
            }
        }
        private final Proxy proxy;
        private final GuardInstance guardInstance;
        private FloatInvoker(GuardInstance guardInstance) {
            this.guardInstance = guardInstance;
            proxy = MethodHandleProxies.asInterfaceInstance(Proxy.class, guardInstance.getTestMethodHandle());
        }
        void invoke(float value) {
            if ( !proxy.test(value) ) {
                guardInstance.guardViolation(value);
            }
        }
        static void invoke(FloatInvoker[] invokers, float value) {
            for( FloatInvoker invoker : invokers ) {
                invoker.invoke(value);
            }
        }
        public interface Proxy {
            boolean test(float value);
        }
    }

    private static final class DoubleInvoker {
        private static final MethodHandle SINGLE;
        private static final MethodHandle MULTI;
        static {
            try {
                SINGLE = LOOKUP.findVirtual(DoubleInvoker.class, "invoke", methodType(void.class, double.class));
                MULTI = LOOKUP.findStatic(DoubleInvoker.class, "invoke", methodType(void.class, DoubleInvoker[].class, double.class));
            }
            catch ( NoSuchMethodException | IllegalAccessException e ) {
                throw new GuardsInternalError(e);
            }
        }
        private final Proxy proxy;
        private final GuardInstance guardInstance;
        private DoubleInvoker(GuardInstance guardInstance) {
            this.guardInstance = guardInstance;
            proxy = MethodHandleProxies.asInterfaceInstance(Proxy.class, guardInstance.getTestMethodHandle());
        }
        void invoke(double value) {
            if ( !proxy.test(value) ) {
                guardInstance.guardViolation(value);
            }
        }
        static void invoke(DoubleInvoker[] invokers, double value) {
            for( DoubleInvoker invoker : invokers ) {
                invoker.invoke(value);
            }
        }
        public interface Proxy {
            boolean test(double value);
        }
    }

    private static final class BooleanInvoker {
        private static final MethodHandle SINGLE;
        private static final MethodHandle MULTI;
        static {
            try {
                SINGLE = LOOKUP.findVirtual(BooleanInvoker.class, "invoke", methodType(void.class, boolean.class));
                MULTI = LOOKUP.findStatic(BooleanInvoker.class, "invoke", methodType(void.class, BooleanInvoker[].class, boolean.class));
            }
            catch ( NoSuchMethodException | IllegalAccessException e ) {
                throw new GuardsInternalError(e);
            }
        }
        private final Proxy proxy;
        private final GuardInstance guardInstance;
        private BooleanInvoker(GuardInstance guardInstance) {
            this.guardInstance = guardInstance;
            proxy = MethodHandleProxies.asInterfaceInstance(Proxy.class, guardInstance.getTestMethodHandle());
        }
        void invoke(boolean value) {
            if ( !proxy.test(value) ) {
                guardInstance.guardViolation(value);
            }
        }
        static void invoke(BooleanInvoker[] invokers, boolean value) {
            for( BooleanInvoker invoker : invokers ) {
                invoker.invoke(value);
            }
        }
        public interface Proxy {
            boolean test(boolean value);
        }
    }

    private static final class CharInvoker {
        private static final MethodHandle SINGLE;
        private static final MethodHandle MULTI;
        static {
            try {
                SINGLE = LOOKUP.findVirtual(CharInvoker.class, "invoke", methodType(void.class, char.class));
                MULTI = LOOKUP.findStatic(CharInvoker.class, "invoke", methodType(void.class, CharInvoker[].class, char.class));
            }
            catch ( NoSuchMethodException | IllegalAccessException e ) {
                throw new GuardsInternalError(e);
            }
        }
        private final Proxy proxy;
        private final GuardInstance guardInstance;
        private CharInvoker(GuardInstance guardInstance) {
            this.guardInstance = guardInstance;
            proxy = MethodHandleProxies.asInterfaceInstance(Proxy.class, guardInstance.getTestMethodHandle());
        }
        void invoke(char value) {
            if ( !proxy.test(value) ) {
                guardInstance.guardViolation(value);
            }
        }
        static void invoke(CharInvoker[] invokers, char value) {
            for( CharInvoker invoker : invokers ) {
                invoker.invoke(value);
            }
        }
        public interface Proxy {
            boolean test(char value);
        }
    }

    private static final class ObjectInvoker {
        private static final MethodHandle SINGLE;
        private static final MethodHandle MULTI;
        static {
            try {
                SINGLE = LOOKUP.findVirtual(ObjectInvoker.class, "invoke", methodType(void.class, Object.class));
                MULTI = LOOKUP.findStatic(ObjectInvoker.class, "invoke", methodType(void.class, ObjectInvoker[].class, Object.class));
            }
            catch ( NoSuchMethodException | IllegalAccessException e ) {
                throw new GuardsInternalError(e);
            }
        }
        private final Proxy proxy;
        private final GuardInstance guardInstance;
        private ObjectInvoker(GuardInstance guardInstance) {
            this.guardInstance = guardInstance;
            proxy = MethodHandleProxies.asInterfaceInstance(Proxy.class, guardInstance.getTestMethodHandle());
        }
        void invoke(Object value) {
            if ( !proxy.test(value) ) {
                guardInstance.guardViolation(value);
            }
        }
        static void invoke(ObjectInvoker[] invokers, Object value) {
            for( ObjectInvoker invoker : invokers ) {
                invoker.invoke(value);
            }
        }
        public interface Proxy {
            boolean test(Object value);
        }
    }
}
