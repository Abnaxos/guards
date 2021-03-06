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

import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.Message;
import ch.raffael.guards.definition.PerformanceImpact;
import ch.raffael.guards.definition.Positioning;


/**
 * Checks that floating point value is real (not NaN, positive or negative infinity).
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Guard(performanceImpact = PerformanceImpact.LOW)
@Positioning(slot = Positioning.Slot.LEADING)
@Message("The value must be a real number")
public @interface Real {

}

/**
 * Guard handler for {@link Real}
 *
 * @see {@link Real}
 */
@SuppressWarnings("unused")
final class RealGuardHandler extends Guard.Handler<Real> {
    public RealGuardHandler(Real annotation) {
        super(annotation);
    }

    public boolean test(float value) {
        return value != Float.NaN
                && value != Float.POSITIVE_INFINITY
                && value != Float.NEGATIVE_INFINITY;
    }
    public boolean test(double value) {
        return value != Double.NaN
                && value != Double.POSITIVE_INFINITY
                && value != Double.NEGATIVE_INFINITY;
    }
}
