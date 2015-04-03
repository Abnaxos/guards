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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ForwardingQueue;


/**
 * A queue that forwards to `LinkedList`, but doesn't accept elements it's seen before a second
 * time. Useful to flatten a recursive hierarchy to a queue of elements to be inspected. It is also
 * null-safe, i.e. adding null simply has no effect.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class InspectionQueue<T> extends ForwardingQueue<T> {

    private final Set<T> seen = new HashSet<>();
    private final LinkedList<T> queue = new LinkedList<>();

    public InspectionQueue() {
    }

    public InspectionQueue(T initialElement) {
        add(initialElement);
    }

    public InspectionQueue(Iterable<? extends T> initialElements) {
        this(initialElements.iterator());
    }

    public InspectionQueue(Collection<? extends T> initialElements) {
        addAll(initialElements);
    }

    public InspectionQueue(Iterator<? extends T> initialElements) {
        while ( initialElements.hasNext() ) {
            add(initialElements.next());
        }
    }

    @Override
    protected Queue<T> delegate() {
        return queue;
    }

    @Override
    public boolean add(T element) {
        return element != null && seen.add(element) && super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean didChange = false;
        for( T element : collection ) {
            if ( add(element) ) {
                didChange = true;
            }
        }
        return didChange;
    }

    @Override
    public boolean offer(T element) {
        return add(element);
    }

}
