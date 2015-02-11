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

import java.lang.annotation.Annotation;
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
@Retention(RetentionPolicy.CLASS)
@Guard(message = "Class not annotated with $value",
        performanceImpact = PerformanceImpact.MEDIUM,
        validate = "allOf.isEmpty() && anyOf.isEmpty && noneOf.isEmpty -> warning: No annotation restrictions specified",
        relations = @RelationRule("todo"))
@Documented
public @interface AnnotatedWith {

    Class<? extends Annotation>[] allOf() default { };

    Class<? extends Annotation>[] anyOf() default { };

    Class<? extends Annotation>[] noneOf() default { };

}
