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

package benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class HelloBenchmark {

    @NotNull
    public static Object test(
            @Nullable
            Object retval,
            @NotNull
            //@Fail
                    Object notNull)
    {
        return retval;
    }

    @Benchmark
    public int helloWorld(HelloState state) {
        int reps = state.reps;
        int result = 0;
        for( int i = 0; i < reps; i++ ) {
            result |= test(new Object(), "foo").hashCode();
        }
        return result;
    }

    @State(Scope.Benchmark)
    public static class HelloState {
        @Param("10000")
        int reps;
    }

}
