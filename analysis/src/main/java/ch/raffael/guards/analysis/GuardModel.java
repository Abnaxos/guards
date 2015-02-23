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

package ch.raffael.guards.analysis;

import java.util.HashSet;
import java.util.Set;

import ch.raffael.guards.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class GuardModel {

    private final Context<?> context;
    private final Object loadLock = new Object();
    private volatile boolean loaded = false;

    private final String name;

    private String message;
    private final Set<Type> supportedTypes = new HashSet<>();

    protected GuardModel(@NotNull Context<?> context, @NotNull String name) {
        this.context = context;
        this.name = name;
    }

    public Context<?> getContext() {
        return context;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getName(NameStyle style) {
        if ( style == NameStyle.SHORT ) {
            int pos = name.lastIndexOf('.');
            if ( pos >= 0 ) {
                return name.substring(pos + 1);
            }
        }
        return name;
    }

    protected abstract void load() throws LoadException;

    private void ensureLoaded() {
        if ( !loaded ) {
            synchronized ( loadLock ) {
                if ( !loaded ) {
                    load();
                    loaded = true;
                }
            }
        }
    }

    private void ensureNotLoaded() {
        if ( loaded ) {
            throw new IllegalStateException("Guard " + this + " has already been loaded");
        }
    }

}
