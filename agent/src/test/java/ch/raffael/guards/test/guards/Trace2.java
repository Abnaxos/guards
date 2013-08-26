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
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.PACKAGE })
@Retention(RetentionPolicy.CLASS)
@Guard(message = "Trace")
@Documented
public @interface Trace2 {

    class Checker extends Guard.Checker<Trace2> {
        public Checker(Trace2 annotation, Guard.Type checkType, Class<?> valueType) {
            super(annotation, checkType, valueType);
        }
        public boolean check(int value) {
            return Trace.Checker.check(int.class, value);
        }
        public boolean check(byte value) {
            return Trace.Checker.check(byte.class, value);
        }
        public boolean check(short value) {
            return Trace.Checker.check(short.class, value);
        }
        public boolean check(long value) {
            return Trace.Checker.check(long.class, value);
        }
        public boolean check(float value) {
            return Trace.Checker.check(float.class, value);
        }
        public boolean check(double value) {
            return Trace.Checker.check(double.class, value);
        }
        public boolean check(char value) {
            return Trace.Checker.check(char.class, value);
        }
        public boolean check(boolean value) {
            return Trace.Checker.check(boolean.class, value);
        }
        public boolean check(Object value) {
            return Trace.Checker.check(Object.class, value);
        }
    }

}
