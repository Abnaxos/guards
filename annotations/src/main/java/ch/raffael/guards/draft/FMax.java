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

package ch.raffael.guards.draft;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.raffael.guards.Positive;
import ch.raffael.guards.Signed;
import ch.raffael.guards.Unsigned;
import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.PerformanceImpact;
import ch.raffael.guards.definition.RelationRule;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Guard(message = "Value must be at most $value",
        performanceImpact = PerformanceImpact.LOW,
        subsetOf = Signed.class,
        relations = {
                @RelationRule({
                        "value > that.value -> subset",
                        "value < that.value -> superset" }),
                @RelationRule(type = FMaxX.class, value = {
                        "value > that.value -> subset",
                        "value <= that.value -> superset" }),
                @RelationRule(type = FMin.class, value = {
                        "value >= that.value -> intersects",
                        "-> disjoint" }),
                @RelationRule(type = FMinX.class, value = {
                        "value > that.value -> intersects",
                        "-> disjoint" }),
                @RelationRule(type = Positive.class, value = {
                        "value == 0 -> superset",
                        "value > 0 -> subset",
                        " -> disjoint" }),
                @RelationRule(type = Unsigned.class, value = {
                        "value == 0 -> equal",
                        "value > 0 -> subset",
                        "-> disjoint" }),
        })
public @interface FMax {

    double value();

}
