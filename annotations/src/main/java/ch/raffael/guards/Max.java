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
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Guard(message = "Value must be at most {value}",
        performanceImpact = PerformanceImpact.LOW,
        relations = {
                @RelationRule({
                        "max>that.max -> subset",
                        "max<that.max -> superset" }),
                @RelationRule(type = Min.class, value = {
                        "max>=that.min -> intersecting",
                        "-> disjoint" }),
                @RelationRule(type = Signed.class, value = {
                        "max == maxValue -> equal",
                        "max >= 0 -> intersecting",
                        "-> disjoint"}),
                @RelationRule(type = Unsigned.class, value = {
                        "max == maxValue -> superset",
                        "max >= 0 -> intersecting",
                        "-> disjoint"}),
                @RelationRule(type = Positive.class, value = {
                        "max == maxValue -> superset",
                        "max >= 1 -> intersecting",
                        "-> disjoint" })})
public @interface Max {

    long value();

}
