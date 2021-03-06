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

package ch.raffael.guards.definition;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@GuardAnnotation
public @interface Relations {

    @GuardAnnotation
    Class<? extends Annotation>[] supersetOf() default {};

    @GuardAnnotation
    Class<? extends Annotation>[] subsetOf() default {};

    @GuardAnnotation
    Class<? extends Annotation>[] equalTo() default {};

    @GuardAnnotation
    Class<? extends Annotation>[] intersectingWith() default {};

    @GuardAnnotation
    Class<? extends Annotation>[] disjointFrom() default {};

    @GuardAnnotation
    Class<? extends Annotation>[] synonymousTo() default {};

    @GuardAnnotation
    Class<? extends Annotation>[] inconsistentWith() default {};

    Relations.Rules[] rules() default {};

    @Target({})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Rules {

        Class<? extends Annotation>[] type() default {};
        String[] value();

    }
}
