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
 * {@link ch.raffael.guards.agent.GuardsTransformer.Mode.ASSERT ASSERT} mode.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class InstrumentationExampleAssert {

    static final CheckerStore $$ch$raffael$guards$checkerStore;
    static final boolean $$ch$raffael$guards$assertionsDisabled;
    static {
        $$ch$raffael$guards$checkerStore = CheckerStore.retrieve(InstrumentationExampleAssert.class.getClassLoader(), "ch.raffael.guards.test.InstrumentationExample");
        $$ch$raffael$guards$assertionsDisabled = !InstrumentationExample.class.desiredAssertionStatus();
    }

    @NotNull // index 0
    public Object foo(@NotNull /* index 1 */ Object bar) {
        String $msg; // no such variable introduced, this will live on the stack
        if ( !$$ch$raffael$guards$assertionsDisabled ) {
            $msg = $$ch$raffael$guards$checkerStore.get(1).check(bar);
            if ( $msg != null ) {
                throw new AssertionError($msg);
            }
        }
        Object retval = null;
        if ( !$$ch$raffael$guards$assertionsDisabled ) {
            $msg = $$ch$raffael$guards$checkerStore.get(0).check(retval);
            if ( $msg != null ) {
                throw new AssertionError($msg);
            }
        }
        return retval;
    }

    // Below the variant in assertion mode.
    // However, the actual code generated will look very similar to the exception variant.
    // If you uncomment this, you'll see what the JVM actually does:
    // static final boolean $assertsionsdisabled =
    //@NotNull // index 0
    //public Object fooWithAssert(@NotNull /* index 1 */ Object bar) {
    //    assert $$ch$raffael$guards$checkerStore.get(1).check(bar) :
    //            "Method object ch.raffael.guards.test.InstrumentationExample#foo(Object bar)\n"
    //                    + "Parameter bar: "
    //                    + $$ch$raffael$guards$checkerStore.get(1).failMessage()
    //                    + "\nValue: " + bar;
    //    Object retval = null;
    //    assert $$ch$raffael$guards$checkerStore.get(0).check(retval) :
    //            "Method object ch.raffael.guards.test.InstrumentationExample#foo(Object bar)\n"
    //                    + "Return value: "
    //                    + $$ch$raffael$guards$checkerStore.get(1).failMessage()
    //                    + "\nValue: " + retval;
    //    return retval;
    //}

}
