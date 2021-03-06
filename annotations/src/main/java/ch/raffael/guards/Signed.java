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
import ch.raffael.guards.definition.PerformanceImpact;
import ch.raffael.guards.definition.Positioning;
import ch.raffael.guards.definition.Relations;


/**
 * Mark a value as signed (opposite of {@link Unsigned @NotNegative}.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Guard(performanceImpact = PerformanceImpact.LOW)
@Relations(supersetOf = { Unsigned.class, Positive.class })
@Positioning(slot = Positioning.Slot.LEADING)
public @interface Signed {

}

/**
 * Guard handler for {@link Signed}
 *
 * @see {@link Signed}
 */
@SuppressWarnings("unused")
final class SignedGuardHandler extends Guard.Handler<Signed> {

    public SignedGuardHandler() {
    }

    public boolean test(int value) {
        return true;
    }

    public boolean test(long value) {
        return true;
    }

    public boolean test(float value) {
        return true;
    }

    public boolean test(double value) {
        return true;
    }

    public boolean test(BigInteger value) {
        return true;
    }

    public boolean test(BigDecimal value) {
        return true;
    }
}
