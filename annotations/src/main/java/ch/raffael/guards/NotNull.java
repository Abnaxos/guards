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
import ch.raffael.guards.definition.Relations;


/**
 * Check that the value is not `null`.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Guard(performanceImpact = PerformanceImpact.LOW, testNulls = true)
@Relations(subsetOf = Nullable.class)
@Positioning(slot = Positioning.Slot.PRIMARY, priority = 1000)
@Message("Value must not be null")
public @interface NotNull {

    int NULLITY_PRIORITY = 1000;

}

/**
 * Guard handler for {@link NotNull}
 *
 * @see {@link NotNull}
 */
@SuppressWarnings("unused")
final class NotNullGuardHandler extends Guard.Handler<NotNull> {

    public static boolean test(Object value) {
        return value != null;
    }

}
