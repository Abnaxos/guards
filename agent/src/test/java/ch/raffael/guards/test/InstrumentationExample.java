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

package ch.raffael.guards.test;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.agent.CheckerStore;


/**
 * This is just an example showing what the instrumented code looks like. It's may be used
 * as reference while coding and debugging the instrumentation, although it's possible
 * javac is in some constructs a tad more verbose than the actual instrumentation.
 *
 * This class illustrates the code generated in
 * {@link ch.raffael.guards.agent.GuardsTransformer.Mode.EXCEPTION EXCEPTION} mode.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class InstrumentationExample {

    static final CheckerStore $$ch$raffael$guards$checkerStore;
    static {
        $$ch$raffael$guards$checkerStore = CheckerStore.retrieve(InstrumentationExample.class.getClassLoader(), "ch.raffael.guards.test.InstrumentationExample");
    }

    @NotNull // index 0
    public Object foo(@NotNull /* index 1 */ Object bar) {
        String $msg; // no such variable introduced, this will live on the stack
        $msg = $$ch$raffael$guards$checkerStore.get(1).check(bar);
        if ( $msg != null ) {
            throw new IllegalArgumentException($msg);
        }
        Object retval = null;
        $msg = $$ch$raffael$guards$checkerStore.get(0).check(retval);
        if ( $msg != null ) {
            throw new IllegalStateException($msg);
        }
        return retval;
    }

}
