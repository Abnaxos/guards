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

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.raffael.guards.definition.GuardAnnotation;


/**
 * Retract a guard.
 *
 * This annotation will retract all inherited guards of the given type for a package, a class
 * or a single method parameter. If applied to an annotation type, this annotation type will
 * act as a synonym.
 *
 * It's impossible to retract guards for method return values accordingly to the rules of
 * design-by-contract: The guard for the return value is a postcondition, as such, it can
 * only be strengthened, but not loosened.Therefore, if applied to a package or class, it
 * will have no effect on return values.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.PARAMETER, ElementType.TYPE, ElementType.PACKAGE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retract {

    @GuardAnnotation
    Class<? extends Annotation>[] value();

}
