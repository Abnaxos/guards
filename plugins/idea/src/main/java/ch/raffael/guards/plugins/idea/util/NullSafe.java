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

package ch.raffael.guards.plugins.idea.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.FluentIterable;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class NullSafe {

    private static final FluentIterable<Object> EMPTY_FLUENT_ITERABLE = FluentIterable.from(Collections.emptyList());

    private NullSafe() {
    }


    @NotNull
    public static <T> FluentIterable<T> fluentIterable(@Nullable Iterable<T> iterable) {
        if ( iterable == null || ((iterable instanceof Collection) && ((Collection)iterable).isEmpty()) ) {
            return fluentIterable();
        }
        else {
            return FluentIterable.from(iterable);
        }
    }

    @SafeVarargs
    @NotNull
    public static <T> FluentIterable<T> fluentIterable(@Nullable T... array) {
        if ( array == null || array.length == 0 ) {
            return fluentIterable();
        }
        else {
            return FluentIterable.from(Arrays.asList(array));
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> FluentIterable<T> fluentIterable() {
        return (FluentIterable<T>)EMPTY_FLUENT_ITERABLE;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T cast(@NotNull Class<T> type, @Nullable Object object) {
        if ( type.isInstance(object) ) {
            return (T)object;
        }
        else {
            return null;
        }
    }
}
