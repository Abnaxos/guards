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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class Context<M extends GuardModel> {

    private static final Map<String, Type> WRAPPER_TYPES;
    static {
        Map<String, Type> wrapperTypes = new HashMap<>();
        wrapperTypes.put("java.lang.Character", Type.CHAR);
        wrapperTypes.put("java.lang.Byte", Type.BYTE);
        wrapperTypes.put("java.lang.Short", Type.SHORT);
        wrapperTypes.put("java.lang.Int", Type.INT);
        wrapperTypes.put("java.lang.Long", Type.LONG);
        wrapperTypes.put("java.lang.Float", Type.FLOAT);
        wrapperTypes.put("java.lang.Double", Type.DOUBLE);
        wrapperTypes.put("java.lang.Boolean", Type.BOOLEAN);
        WRAPPER_TYPES = Collections.unmodifiableMap(wrapperTypes);
    }

    private final Map<String, Type> types = new HashMap<>();

    private final Map<String, M> guards = new HashMap<>();

    public Context() {
    }

    private void putBuiltinType(@NotNull Type type) {
        types.put(type.getName(), type);
    }

    private final Set<String> typeLoadTracker = new LinkedHashSet<>();
    @Nullable
    public Type getType(String name) {
        synchronized ( types ) {
            if ( types.isEmpty() ) {
                putBuiltinType(Type.CHAR);
                putBuiltinType(Type.BYTE);
                putBuiltinType(Type.SHORT);
                putBuiltinType(Type.INT);
                putBuiltinType(Type.LONG);
                putBuiltinType(Type.FLOAT);
                putBuiltinType(Type.DOUBLE);
                putBuiltinType(Type.BOOLEAN);
                putBuiltinType(Type.OBJECT);
            }
            if ( types.containsKey(name) ) {
                return types.get(name);
            }
            if ( !typeLoadTracker.add(name) ) {
                throw new IllegalStateException("Recursion detected loading types: " + typeLoadTracker + " -> " + name);
            }
            try {
                Type type = findType(name);
                if ( type != null ) {
                    if ( !name.equals(type.getName()) ) {
                        throw new LoadException("Request for type " + name + " resulted in type " + type.getName());
                    }
                    Type wrapperFor = WRAPPER_TYPES.get(type.getName());
                    if ( wrapperFor != null ) {
                        type = new Type.WrapperType(type, wrapperFor);
                    }
                }
                types.put(name, type);
                return type;
            }
            finally {
                typeLoadTracker.remove(name);
            }
        }
    }

    @Nullable
    protected abstract Type findType(@NotNull String name);

    private final Set<String> guardLoadTracker = new LinkedHashSet<>();
    @Nullable
    public GuardModel getGuard(@NotNull String name) {
        synchronized ( guards ) {
            if ( guards.containsKey(name) ) {
                return guards.get(name);
            }
            if ( !guardLoadTracker.add(name) ) {
                throw new IllegalStateException("Recursion detected loading guards: " + guardLoadTracker + " -> " + name);
            }
            try {
                M guard = findGuard(name);
                if ( guard != null ) {
                    if ( !name.equals(guard.getName()) ) {
                        throw new LoadException("Request for guard " + name + " resulted in guard " + guard.getName());
                    }
                }
                guards.put(name, guard);
                return guard;
            }
            finally {
                guardLoadTracker.remove(name);
            }
        }
    }

    @NotNull
    public M add(@NotNull M model) {
        synchronized ( guards ) {
            M current = guards.get(model.getName());
            if ( current == null ) {
                guards.put(model.getName(), model);
                return model;
            }
            else {
                return current;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<M> getKnownGuards() {
        synchronized ( guards ) {
            return (Collection<M>)Collections.unmodifiableCollection(Arrays.asList(guards.values().toArray(new GuardModel[guards.values().size()])));
        }
    }

    @Nullable
    protected abstract M findGuard(@NotNull String name);

    @NotNull
    public GuardModel requireGuard(@NotNull String name) {
        GuardModel model = getGuard(name);
        if ( model == null ) {
            throw new LoadException("No such guard: " + name);
        }
        return model;
    }

}
