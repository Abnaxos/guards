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

package ch.raffael.guards.agent;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.agent.guava.collect.ImmutableList;
import ch.raffael.guards.agent.guava.collect.ImmutableSet;
import ch.raffael.guards.definition.Guard;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class GuardAnnotationInspector {

    private static final ImmutableSet<Class<? extends Annotation>> KNOWN_NON_GUARDS = ImmutableSet.of(
            Target.class, Retention.class, Inherited.class, Documented.class
    );

    private final Class<? extends Annotation> origin;
    private final Map<Class<? extends Annotation>, Entry> entries = new HashMap<>();


    GuardAnnotationInspector(@NotNull Class<? extends Annotation> origin) {
        this.origin = origin;
    }

    boolean isGuard() throws Circularity {
        if ( isKnownNonGuard(origin) ) {
            return false;
        }
        entries.clear();
        Entry originEntry = entry(origin);
        buildGraph(originEntry);
        if ( !originEntry.isGuard ) {
            return false;
        }
        checkCircularity(origin, new LinkedList<Class<? extends Annotation>>());
        return true;
    }

    private void buildGraph(@NotNull Entry entry) {
        for( Annotation annotation : entry.type.getAnnotations() ) {
            if ( isKnownNonGuard(annotation.annotationType()) ) {
                continue;
            }
            entry(annotation.annotationType()).addAnnotated(entry);
        }
    }

    private void checkCircularity(Class<? extends Annotation> type, LinkedList<Class<? extends Annotation>> path) throws Circularity {
        int index = path.indexOf(type);
        if ( index >= 0 ) {
            path.add(type);
            // todo: also possible: throw new Circularity(ImmutableList.copyOf(path.subList(index, path.size() - 1)));
            throw new Circularity(ImmutableList.copyOf(path));
        }
        for( Annotation annotation : type.getAnnotations() ) {
            if ( isKnownNonGuard(annotation.annotationType()) ) {
                continue;
            }
            if ( !entry(annotation.annotationType()).isGuard ) {
                continue;
            }
            try {
                path.addLast(type);
                checkCircularity(annotation.annotationType(), path);
            }
            finally {
                path.removeLast();
            }
        }
    }

    @NotNull
    Entry entry(@NotNull Class<? extends Annotation> type) {
        Entry entry = entries.get(type);
        if ( entry == null ) {
            entry = new Entry(type);
            entries.put(type, entry);
            buildGraph(entry);
        }
        return entry;
    }

    private boolean isKnownNonGuard(Class<? extends Annotation> type) {
        return KNOWN_NON_GUARDS.contains(type);
    }

    final class Entry {
        final Class<? extends Annotation> type;
        final Set<Entry> annotated = new HashSet<>();
        boolean isGuard = false;
        Entry(@NotNull Class<? extends Annotation> type) {
            this.type = type;
            if ( type.getAnnotation(Guard.class) != null ) {
                isGuard = true;
            }
        }
        boolean addAnnotated(Entry entry) {
            if ( annotated.add(entry) ) {
                if ( isGuard ) {
                    entry.markAsGuard();
                }
                return true;
            }
            else {
                return false;
            }
        }
        void markAsGuard() {
            if ( !isGuard ) {
                isGuard = true;
                for( Entry e : annotated ) {
                    e.markAsGuard();
                }
            }
        }
    }

    static class Circularity extends Exception {
        final List<Class<? extends Annotation>> path;
        Circularity(List<Class<? extends Annotation>> path) {
            this.path = path;
        }
    }

}
