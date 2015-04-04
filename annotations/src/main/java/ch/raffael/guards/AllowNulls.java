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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.Message;
import ch.raffael.guards.definition.PerformanceImpact;
import ch.raffael.guards.definition.Positioning;
import ch.raffael.guards.definition.Relations;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Guard(performanceImpact = PerformanceImpact.LOW)
@Message("Collection mut not contain null values")
@Relations(supersetOf = NoNulls.class)
@Positioning(slot = Positioning.Slot.PRIMARY)
@Retract(NoNulls.class)
public @interface AllowNulls {

}

/**
 * Guard handler for {@link AllowNulls}
 *
 * @see {@link AllowNulls}
 */
@SuppressWarnings("unused")
final class AllowNullsGuardHandler extends Guard.Handler<AllowNulls> {

    public AllowNullsGuardHandler() {
    }

    // TODO: Include Iterator/Iterable?
    //public boolean test(Iterable<?> iterable) {
    //    for ( Object elem : iterable ) {
    //        if ( elem == null ) {
    //            return false;
    //        }
    //    }
    //    return true;
    //}

    public boolean test(Collection<?> collection) {
            for ( Object elem : collection ) {
                    if ( elem == null ) {
                            return false;
                    }
            }
            return true;
    }

    public boolean test(Object[] array) {
            for ( Object element : array ) {
                    if ( element == null ) {
                            return false;
                    }
            }
            return true;
    }
}
