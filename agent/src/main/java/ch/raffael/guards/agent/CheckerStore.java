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

package ch.raffael.guards.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.MapMaker;

import ch.raffael.guards.GuardsInternalError;
import ch.raffael.guards.definition.Guard;


/**
 * Stores the {@link Guard.Checker Checkers} for a class. For nested, inner and local
 * classes, the `Checker`s will be stored in the `CheckerStore` of the outermost class.
 * The code generated looks like that:
 *
 * ```java
 * static final CheckerStore $$ch$raffael$guards$checkerStore =
 *     CheckerStore.retrieve("my.pkg.MyClass", MyClass.class.getClassLoader());
 * ```
 *
 * During instrumentation, the `CheckerStore` will be created and filled with the checkers
 * needed. The checkers are temporarily stored in a map so `retrieve()` can retrieve the
 * instance upon class initialization. `retrieve()` may only be called once for a given
 * `CheckerStore`. After that, instrumentation will use reflection to access the
 * `CheckerStore` if needed (e.g. to add more checkers for inner classes).
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class CheckerStore {

    private static final Map<ClassLoader, Map<String, CheckerStore>> STORES =
            new MapMaker().weakKeys().concurrencyLevel(2).makeMap();

    static CheckerStore storeFor(ClassLoader loader, String className) {
        synchronized ( STORES ) {
            Map<String, CheckerStore> storesForLoader = STORES.get(loader);
            if ( storesForLoader == null ) {
                storesForLoader = new HashMap<>();
                STORES.put(loader, storesForLoader);
            }
            CheckerStore store = storesForLoader.get(className);
            if ( store == null ) {
                store = new CheckerStore(loader);
                storesForLoader.put(className, store);
            }
            return store;
        }
    }

    public static CheckerStore retrieve(ClassLoader loader, String className) {
        synchronized ( STORES ) {
            Map<String, CheckerStore> storesForLoader = STORES.get(loader);
            if ( storesForLoader == null ) {
                return null;
            }
            CheckerStore store = storesForLoader.get(className);
            if ( store == null ) {
                throw new GuardsInternalError("No CheckerStore found for" + loader + "::" + className);
            }
            return store;
        }
    }

    private final ClassLoader loader;
    private final Object addLock = new Object();
    private final CopyOnWriteArrayList<CheckerBridge> checkers = new CopyOnWriteArrayList<>();

    private CheckerStore(ClassLoader loader) {
        this.loader = loader;
    }

    int add(CheckerBridge bridge) {
        synchronized ( addLock ) {
            bridge.checkerStore = this;
            bridge.index = checkers.size();
            checkers.add(bridge);
            return bridge.index;
        }
    }

    void invalidate(int index) {
        checkers.set(index, null);
    }

    public CheckerBridge get(int index) {
        CheckerBridge checker = checkers.get(index);
        checker.initialize();
        return checker;
    }

    ClassLoader loader() {
        return loader;
    }

}
