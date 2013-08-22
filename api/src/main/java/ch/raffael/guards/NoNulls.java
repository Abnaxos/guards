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

import ch.raffael.guards.definition.Disabler;
import ch.raffael.guards.definition.Guard;


/**
 * Check that an {@link Iterable} or array does not contain any `null`
 * values.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
@Guard(message = "Iterable may not contain null elements")
@Documented
public @interface NoNulls {

    @Disabler
    boolean skipCheck() default false;

    class Checker extends Guard.Checker<NoNulls> {
        public Checker(NoNulls annotation, Guard.Type type, Class<?> valueType)  {
            super(annotation, type, valueType);
        }
        public boolean check(Iterable<?> iterable) {
            for ( Object elem : iterable ) {
                if ( elem == null ) {
                    return false;
                }
            }
            return true;
        }
        public boolean check(Object[] array) {
            for ( Object element : array ) {
                if ( element == null ) {
                    return false;
                }
            }
            return true;
        }
    }

}
