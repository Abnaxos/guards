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

package ch.raffael.guards;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

import ch.raffael.guards.definition.Guard;


/**
 * Check that a {@link Collection}, {@link Iterable}, {@link CharSequence} or array is not
 * empty.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
@Guard(message = "Value may not be empty")
@Documented
public @interface NotEmpty {

    class Checker extends Guard.Checker<NotEmpty> {
        public Checker(NotEmpty annotation, Guard.Type checkType, Class<?> valueType) {
            super(annotation, checkType, valueType);
        }
        public boolean check(CharSequence string) {
            return string.length() > 0;
        }
        public boolean check(Iterable<?> iterable) {
            return iterable.iterator().hasNext();
        }
        public boolean check(Collection<?> collection) {
            return !collection.isEmpty();
        }
        public boolean check(Object[] array) {
            return array.length > 0;
        }
        public boolean check(int[] array) {
            return array.length > 0;
        }
        public boolean check(byte[] array) {
            return array.length > 0;
        }
        public boolean check(short[] array) {
            return array.length > 0;
        }
        public boolean check(long[] array) {
            return array.length > 0;
        }
        public boolean check(float[] array) {
            return array.length > 0;
        }
        public boolean check(double[] array) {
            return array.length > 0;
        }
        public boolean check(char[] array) {
            return array.length > 0;
        }
        public boolean check(boolean[] array) {
            return array.length > 0;
        }
    }

}
