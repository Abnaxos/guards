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

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import org.jetbrains.annotations.Contract;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class Lazy<T> implements Supplier<T> {

    private final Object lock;
    private volatile Optional<T> value;

    public Lazy() {
        this(null);
    }

    public Lazy(@Nullable Object lock) {
        this.lock = lock == null ? new Object() : lock;
    }

    @Override
    @NotNull
    public T get() {
        return init().get();
    }

    @Contract("!null -> !null")
    @Nullable
    public T get(@Nullable T fallback) {
        if ( value == null ) {
            synchronized ( lock ) {
                if ( value == null ) {
                    value = Optional.fromNullable(create());
                }
            }
        }
        return value.or(fallback);
    }

    public Supplier<T> supplier(@Nullable final T fallback) {
        return new Supplier<T>() {
            @Override
            public T get() {
                return Lazy.this.get(fallback);
            }
        };
    }

    public boolean isLoaded() {
        return value != null;
    }

    public Optional<T> optional() {
        return init();
    }

    @NotNull
    private Optional<T> init() {
        if ( value == null ) {
            synchronized ( lock ) {
                if ( value == null ) {
                    value = Optional.fromNullable(create());
                }
            }
        }
        return value;
    }

    @Nullable
    protected abstract T create();

}
