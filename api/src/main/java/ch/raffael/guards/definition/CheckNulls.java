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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks a checker method should also receive `null` values.
 *
 * Usually, `null` values will not be passed on to the checker method, `null` is always
 * considered a valid value for the guard. This is usually the right thing to do as there
 * is a {@link ch.raffael.guards.NotNull @NotNull} annotation to disable `null` values.
 *
 * However, there may be exceptions to this, notably the
 * {@link ch.raffael.guards.NotNull @NotNull} annotation itself.
 *
 * Checker methods annotated with `@CheckNulls` will also receive `null` values.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckNulls {

}
