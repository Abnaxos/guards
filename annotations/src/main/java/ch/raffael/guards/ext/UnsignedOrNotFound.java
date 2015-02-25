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

package ch.raffael.guards.ext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.raffael.guards.Min;
import ch.raffael.guards.definition.Message;
import ch.raffael.guards.definition.Positioning;
import ch.raffael.guards.definition.PositioningTendency;


/**
 * The number must be unsigned (>=0) or -1 if not found. The `indexOf(...)` methods are a good
 * example of where to use this.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented

@Min(-1)
@Positioning(value = PositioningTendency.LEADING)
@Message("The value must be signed or -1 if not found")
public @interface UnsignedOrNotFound {

}
