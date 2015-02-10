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

import ch.raffael.guards.definition.ComplexRelation;
import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.PerformanceImpact;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Guard(message = "Value must be in range $min (inclusive: $minInclusive) to $max (inclusive: $maxInclusive)",
        performanceImpact = PerformanceImpact.LOW,
        validate = {
                "min >= max -> error: $min must be less than $max"
        },
        complexRelations = {
                @ComplexRelation({
                        "(minInclusive ? min<=that.min : min< && max>=that.max -> superset",
                        "min>=that.min && max<=that.max -> subset"
                }),
                @ComplexRelation(type=IntRange.class, value = {

                })})
public @interface FloatRange {

    double min() default 0;

    boolean minInclusive() default true;

    double max() default Double.POSITIVE_INFINITY;

    boolean maxInclusive() default true;

}
