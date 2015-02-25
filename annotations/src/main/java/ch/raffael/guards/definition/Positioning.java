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
 * Describes a positioning tendency for guards. Guards will be positioned by IDE support and the
 * agent when applying the guards. It describes "slots" where the annotations will be placed. It
 * also provides means to group several annotations together, like {@link ch.raffael.guards.Min @Min}
 * always right next to {@link ch.raffael.guards.Max @Max}.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@GuardAnnotation
public @interface Positioning {

    PositioningTendency value() default PositioningTendency.PRIMARY;

    Class<? extends Annotation>[] before() default {};

    Class<? extends Annotation>[] after() default {};

    /**
     * Special value for {@link Positioning#before() before} and {@link Positioning#after() after}
     * to indicate that the annotation should be placed before all/after all other annotations in
     * its {@link Positioning#value() tendency} group.
     */
    @Target({})
    @Retention(RetentionPolicy.RUNTIME)
    @interface All {}

}
