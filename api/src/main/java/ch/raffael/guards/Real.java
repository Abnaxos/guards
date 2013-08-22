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

import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.util.FloatUnboxingChecker;


/**
 * Checks that floating point value is real (not NaN, positive or negative infinity).
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.CLASS)
@Guard(message = "The value must be a real number.")
@Documented
public @interface Real {

    class Checker extends FloatUnboxingChecker<Real> {
        public Checker(Real annotation, Guard.Type checkType, Class<?> valueType) {
            super(annotation, checkType, valueType);
        }
        @Override
        public boolean check(float value) {
            return value != Float.NaN
                    && value != Float.POSITIVE_INFINITY
                    && value != Float.NEGATIVE_INFINITY;
        }
        @Override
        public boolean check(double value) {
            return value != Double.NaN
                    && value != Double.POSITIVE_INFINITY
                    && value != Double.NEGATIVE_INFINITY;
        }
    }

}
