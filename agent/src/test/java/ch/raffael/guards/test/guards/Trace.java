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

package ch.raffael.guards.test.guards;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.raffael.guards.definition.Guard;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.CLASS)
@Guard(message = "Trace")
@Documented
public @interface Trace {

    class Checker extends Guard.Checker<Trace> {
        static final ThreadLocal<Tracer> TRACER = new ThreadLocal<>();
        public Checker(Trace annotation, Guard.Type checkType, Class<?> valueType) {
            super(annotation, checkType, valueType);
        }
        public static void tracer(Tracer tracer) {
            TRACER.set(tracer);
        }
        public boolean check(int value) {
            return check(int.class, value);
        }
        public boolean check(byte value) {
            return check(byte.class, value);
        }
        public boolean check(short value) {
            return check(short.class, value);
        }
        public boolean check(long value) {
            return check(long.class, value);
        }
        public boolean check(float value) {
            return check(float.class, value);
        }
        public boolean check(double value) {
            return check(double.class, value);
        }
        public boolean check(char value) {
            return check(char.class, value);
        }
        public boolean check(boolean value) {
            return check(boolean.class, value);
        }
        public boolean check(Object value) {
            return check(Object.class, value);
        }
        public static boolean check(Class type, Object value) {
            Tracer tracer = TRACER.get();
            if ( tracer != null ) {
                tracer.check(type, value);
            }
            return true;
        }
    }

    interface Tracer {
        void check(Class type, Object value);
    }

}
