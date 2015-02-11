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
import ch.raffael.guards.definition.PerformanceImpact;
import ch.raffael.guards.definition.RelationRule;


/**
 * Checks that a (numeric) value is in the specified integer range.
 *
 * @deprecated Use `@Max` and `@Min` instead; kept for demonstrating complex guard relations.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Guard(message = "Integer value must be within the range [$min,$max]",
        performanceImpact = PerformanceImpact.LOW,
        validate = {
                "min >= max -> error: $min must be less than $max"
        },
        relations = @RelationRule({
                "min<=that.min && max>=that.max -> superset",
                "min>=that.min && max<=that.max -> subset"
                /* implicit: "-> incompatible" */ }))
@Deprecated
public @interface IntRange {

    long min() default 0;

    long max() default Long.MAX_VALUE;

}
