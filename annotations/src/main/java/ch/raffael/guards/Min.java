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

package ch.raffael.guards;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.math.BigInteger;

import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.Message;
import ch.raffael.guards.definition.PerformanceImpact;
import ch.raffael.guards.definition.Positioning;
import ch.raffael.guards.definition.Relations;
import ch.raffael.guards.definition.Relations.Rules;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Guard(performanceImpact = PerformanceImpact.LOW)
@Relations(rules = {
        @Rules({
                "value > that.value -> subset",
                "value < that.value -> superset" }),
        @Rules(type = Max.class, value = {
                "value <= that.value -> intersecting",
                "-> disjoint" }),
        @Rules(type = Signed.class, value = {
                "value == minValue -> equal",
                "-> subset" }),
        @Rules(type = Unsigned.class, value = {
                "value > 0 -> subset",
                "value == 0 -> equal",
                "-> superset" }),
        @Rules(type = Positive.class, value = {
                "value > 1 -> subset",
                "value == 1 -> equal",
                "-> superset" })
})
@Positioning(slot = Positioning.Slot.LEADING, groupBefore = Max.class)
@Message("Value must be at least {value}")
public @interface Min {

    long value();

}

/**
 * Guard handler for {@link Min}
 *
 * @see {@link Min}
 */
@SuppressWarnings("unused")
final class MinGuardHandler extends Guard.Handler<Min> {

    private final int minInt;
    private final long min;

    public MinGuardHandler(Min annotation) {
            super(annotation);
            min = annotation.value();
            minInt = min > Integer.MIN_VALUE ? (int)min : Integer.MIN_VALUE;
    }

    public boolean test(int value) {
            return value >= minInt;
    }

    public boolean test(long value) {
            return value >= min;
    }

    public boolean test(float value) {
            return value >= min;
    }

    public boolean test(double value) {
            return value >= min;
    }

    public boolean test(BigInteger value) {
            return value.compareTo(BigInteger.valueOf(min)) >= 0;
    }

    public boolean test(BigDecimal value) {
            return value.compareTo(BigDecimal.valueOf(min)) >= 0;
    }
}
