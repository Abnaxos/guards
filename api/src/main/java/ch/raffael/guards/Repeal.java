/*
 * Copyright 2013 Raffael Herzog
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


/**
 * Repeal a guard.
 *
 * If applied to a parameter or method, this annotation has no actual effect as the agent
 * only applies guards that are declared on the method being instrumented directly,
 * inheritance is not supported by the agent. However, it may serve as documentation and
 * static code analysis tools (e.g. a future IDEA plugin) may use it.
 *
 * If applied to an annotation type, this annotation marks the annotated type as reverse
 * of the specified guard annotations. For example {@link Nullable @Nullable} repeals
 * {@link NotNull @NotNull}.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface Repeal {

    Class<? extends Annotation>[] value();

}
