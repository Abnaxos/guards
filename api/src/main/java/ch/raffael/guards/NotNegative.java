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
import java.math.BigDecimal;
import java.math.BigInteger;

import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.util.NumberUnboxingChecker;


/**
 * Check that a number is not negative (`value>=0`).
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.CLASS)
@Guard(message = "Value must not be negative")
@Documented
public @interface NotNegative {

    class Checker extends NumberUnboxingChecker<NotNegative> {
        private static final BigDecimal DEC_ZERO = new BigDecimal(0);
        private static final BigInteger BIGINT_ZERO = new BigInteger("0");
        public Checker(NotNegative annotation, Guard.Type type, Class<?> valueType) {
            super(annotation, type, valueType);
        }
        public boolean check(int value) {
            return value >= 0;
        }
        public boolean check(long value) {
            return value >= 0;
        }
        public boolean check(float value) {
            return value >= 0;
        }
        public boolean check(double value) {
            return value >= 0;
        }
        public boolean check(BigDecimal value) {
            return DEC_ZERO.compareTo(value) >= 0;
        }
        public boolean check(BigInteger value) {
            return BIGINT_ZERO.compareTo(value) >= 0;
        }
    }

}
