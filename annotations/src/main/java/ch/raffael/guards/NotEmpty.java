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
 * Check that a {@link Collection}, {@link Iterable}, {@link CharSequence} or array is not empty.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Guard(performanceImpact = PerformanceImpact.LOW)
@Relations(supersetOf = { NoNulls.class, AllowNulls.class })
@Positioning(slot = Positioning.Slot.LEADING, groupBefore = Max.class)
@Message("Value may not be empty")
public @interface NotEmpty {

}

/**
 * Guard handler for {@link NotEmpty}
 *
 * @see {@link NotEmpty}
 */
@SuppressWarnings("unused")
final class NotEmptyGuardHandler extends Guard.Handler<NotEmpty> {

    public NotEmptyGuardHandler(NotEmpty annotation) {
        super(annotation);
    }

    public boolean test(CharSequence string) {
        return string.length() > 0;
    }

    // TODO: include Iterable/Iterator?
    //public boolean test(Iterable<?> iterable) {
    //    return iterable.iterator().hasNext();
    //}

    public boolean test(Collection<?> collection) {
        return !collection.isEmpty();
    }

    public boolean test(Object[] array) {
        return array.length > 0;
    }

    public boolean test(int[] array) {
        return array.length > 0;
    }

    public boolean test(byte[] array) {
        return array.length > 0;
    }

    public boolean test(short[] array) {
        return array.length > 0;
    }

    public boolean test(long[] array) {
        return array.length > 0;
    }

    public boolean test(float[] array) {
        return array.length > 0;
    }

    public boolean test(double[] array) {
        return array.length > 0;
    }

    public boolean test(char[] array) {
        return array.length > 0;
    }

    public boolean test(boolean[] array) {
        return array.length > 0;
    }
}
