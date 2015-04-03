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

import java.util.Iterator;

import com.google.common.collect.Iterators;
import com.intellij.util.containers.FluentIterable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class FluentIterables {

    private FluentIterables() {
    }

    public static <T> FluentIterable<T> head(final boolean include, final T element, final FluentIterable<T> delegate) {
        if ( include ) {
            return new FluentIterable<T>() {
                @Override
                public Iterator<T> iterator() {
                    return Iterators.concat(Iterators.singletonIterator(element), delegate.iterator());
                }
            };
        }
        else {
            return delegate;
        }
    }

}
