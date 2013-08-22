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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.raffael.guards.definition.CheckNulls;
import ch.raffael.guards.definition.Guard;


/**
 * Check that the value is not `null`.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.PACKAGE })
@Retention(RetentionPolicy.CLASS)
@Documented
@Guard(message = "Value must not be null")
public @interface NotNull {

    class Checker extends Guard.Checker<NotNull> {

        public Checker(NotNull annotation, Guard.Type type, Class<?> valueType) {
            super(annotation, type, valueType);
        }

        @CheckNulls
        public boolean check(Object value) {
            return value != null;
        }
    }

}
