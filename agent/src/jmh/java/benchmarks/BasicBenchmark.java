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
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Unsigned;
import ch.raffael.guards.agent.GuardsAgent;
import ch.raffael.guards.runtime.ContractViolationError;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class BasicBenchmark {

    public static final int REPS = 10000;

    @SuppressWarnings("ConstantConditions")
    @Unsigned
    public static int sanityCheckMethod(@Unsigned int input) {
        return -1;
    }

    @SuppressWarnings("ConstantConditions")
    @Unsigned
    public int allAnnotatedMethod(@Unsigned int prevValue, @Unsigned int value, @NotNull Object notNull) {
        return prevValue | value;
    }

    @SuppressWarnings("ConstantConditions")
    @Unsigned
    public int nonAnnotatedMethod(@Unsigned int prevValue, @Unsigned int value, @NotNull Object notNull) {
        return prevValue | value;
    }

    @Benchmark
    @OperationsPerInvocation(REPS)
    public int allAnnotated(Blackhole bh, MyState state) {
        int result = 0;
        for( int i = 0; i < REPS; i++ ) {
            result = allAnnotatedMethod(result, i, "foo");
        }
        return bh.hashCode();
    }

    @Benchmark
    @OperationsPerInvocation(REPS)
    public int noAnnotations(Blackhole bh, MyState state) {
        int result = 0;
        for( int i = 0; i < REPS; i++ ) {
            result = nonAnnotatedMethod(result, i, "foo");
        }
        return bh.hashCode();
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @State(Scope.Benchmark)
    public static class MyState {

        @SuppressWarnings("ConstantConditions")
        @Setup
        public void checkForAgent() {
            if ( GuardsAgent.getInstance().isInstalled() && !GuardsAgent.getInstance().getOptions().isNopMode() ) {
                try {
                    sanityCheckMethod(1);
                    throw new RuntimeException("Expected ContractViolationError not thrown");
                }
                catch ( ContractViolationError e ) {
                    System.out.println("Sanity Check: " + e);
                    if ( !e.getMessage().contains(":return\n") ) {
                        throw new RuntimeException("Thrown ContractViolationError did not meet expectations");
                    }
                }
                try {
                    sanityCheckMethod(-1);
                    throw new RuntimeException("Expected ContractViolationError not thrown");
                }
                catch ( ContractViolationError e ) {
                    System.out.println("Sanity Check: " + e);
                    if ( e.getMessage().contains(":return\n") ) {
                        throw new RuntimeException("Thrown ContractViolationError did not meet expectations");
                    }
                }
            }
            else {
                try {
                    sanityCheckMethod(1);
                    sanityCheckMethod(-1);
                    System.out.println("Guard violations were unblamably accepted");
                }
                catch ( ContractViolationError e ) {
                    System.out.println("Unexpected contract violation (agent assumed to be inactive): " + e.getMessage());
                    throw e;
                }
            }
        }
    }

}
