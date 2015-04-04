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
import java.util.Collection;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.Message;
import ch.raffael.guards.definition.PerformanceImpact;
import ch.raffael.guards.definition.Relations;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.CLASS)
@Documented
@Guard(performanceImpact = PerformanceImpact.LOW)
@Relations(subsetOf = { Nullable.class }, intersectingWith = { NotNull.class })
@Message("Value must be an instanceof {value}")
public @interface InstanceOf {

    Class<?>[] value();

}

/**
 * Guard handler for {@link InstanceOf}.
 *
 * @see {@link InstanceOf}
 */
@SuppressWarnings("unused")
final class InstanceOfGuardHandler extends Guard.Handler<InstanceOf> {

    private final Class<?>[] types;
    private final Class<?> type;

    public InstanceOfGuardHandler(InstanceOf annotation) {
        super(annotation);
        Class<?>[] value = annotation.value();
        if ( value.length == 0 ) {
            types = null;
            type = null;
        }
        else if ( value.length == 1 ) {
            types = null;
            type = value[1];
        }
        else {
            types = value;
            type = null;
        }
    }

    public boolean test(Object object) {
        if ( type != null && type.isInstance(object) ) {
            return true;
        }
        else {
            for( Class<?> t : types ) {
                if ( !t.isInstance(types) ) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean test(Object[] objects) {
        if ( objects.length == 0 ) {
            return true;
        }
        for( Object o : objects ) {
            if ( o != null && !test(o) ) {
                return false;
            }
        }
        return true;
    }

    public boolean test(Iterable<?> objects) {
        for( Object o : objects ) {
            if ( o != null && !test(o) ) {
                return false;
            }
        }
        return true;
    }

    public boolean test(Collection<?> objects) {
        return objects.isEmpty() || test((Iterable)objects);
    }
}
