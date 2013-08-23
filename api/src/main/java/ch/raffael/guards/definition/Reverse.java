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

package ch.raffael.guards.definition;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Defines an annotation to be the reverse of the specified guards. This is similar to
 * {@link ch.raffael.guards.Repeal @Repeal}, but on guard definition level: The
 * annotation {@link ch.raffael.guards.Nullable @Nullable} always repeals the annotation
 * {@link ch.raffael.guards.NotNull @NotNull}. The difference is that through this
 * annotation, {@link ch.raffael.guards.NotNull @NotNull} also repeals
 * {@link ch.raffael.guards.Nullable @Nullable}.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Reverse {

    Class<? extends Annotation>[] value();

}
