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

import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.Index;
import ch.raffael.guards.definition.util.FloatUnboxingChecker;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.CLASS)
@Guard(message = "Value must be in range %e (inclusive: %s) to %e (inclusive: %s)")
@Documented
public @interface FloatRange {

    @Index(0)
    double min() default 0;

    @Index(1)
    boolean minInclusive() default true;

    @Index(2)
    double max() default Double.POSITIVE_INFINITY;

    @Index(3)
    boolean maxInclusive() default true;

    class Checker extends FloatUnboxingChecker<FloatRange> {

        private final BigDecimal decMin;
        private final BigDecimal decMax;

        public Checker(FloatRange annotation, Guard.Type checkType, Class<?> valueType) {
            super(annotation, checkType, valueType);
            if ( BigDecimal.class.isAssignableFrom(valueType) ) {
                decMin = new BigDecimal(annotation.min());
                decMax = new BigDecimal(annotation.max());
            }
            else {
                decMin = decMax = null;
            }
        }

        @Override
        public boolean check(double value) {
            if ( annotation.minInclusive() ) {
                if ( !(value >= annotation.min()) ) {
                    return false;
                }
            }
            else {
                if ( !(value > annotation.min()) ) {
                    return false;
                }
            }
            if ( annotation.maxInclusive() ) {
                if ( !(value <= annotation.min()) ) {
                    return false;
                }
            }
            else {
                if ( !(value < annotation.min()) ) {
                    return false;
                }
            }
            return false;
        }

        public boolean check(BigDecimal value) {
            if ( annotation.minInclusive() ) {
                if ( !(value.compareTo(decMin) >= 0) ) {
                    return false;
                }
            }
            else {
                if ( !(value.compareTo(decMin) > 0) ) {
                    return false;
                }
            }
            if ( annotation.maxInclusive() ) {
                if ( !(value.compareTo(decMax) <= 0) ) {
                    return false;
                }
            }
            else {
                if ( !(value.compareTo(decMax) < 0) ) {
                    return false;
                }
            }
            return false;
        }

    }

}
