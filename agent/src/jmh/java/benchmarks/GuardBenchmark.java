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

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Unsigned;
import ch.raffael.guards.agent.GuardsAgent;
import ch.raffael.guards.agent.guava.base.Stopwatch;
import ch.raffael.guards.agent.guava.io.Files;
import ch.raffael.guards.runtime.ContractViolationError;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings({ "UnusedParameters", "UseOfSystemOutOrSystemErr" })
public class GuardBenchmark {

    public static final int REPS = 10000;

    public static int guardUnsigned(int value) {
        if ( value < 0 ) {
            throw new ContractViolationError("Manual guard");
        }
        return value;
    }

    public static Object guardNotNull(Object value) {
        if ( value == null ) {
            throw new ContractViolationError("Manual guard");
        }
        return value;
    }

    public static int guardExclude1(int value) {
        if ( value == -1 ) {
            throw new ContractViolationError("Manual guard");
        }
        return value;
    }

    public static int guardExclude2(int value) {
        if ( value == -2 ) {
            throw new ContractViolationError("Manual guard");
        }
        return value;
    }

    public static int guardExclude3(int value) {
        if ( value == -3 ) {
            throw new ContractViolationError("Manual guard");
        }
        return value;
    }

    @SuppressWarnings("ConstantConditions")
    @Unsigned
    public static int sanityCheckMethod(@Unsigned int input) {
        return -1;
    }

    public static int multiSanityCheckMethod(@Exclude1 @Exclude2 @Exclude3 int input) {
        return input;
    }

    public int notGuardedMethod(int prevValue, int value, Object notNull) {
        return prevValue | value;
    }

    public int manuallyGuardedMethod(int prevValue, int value, Object notNull) {
        guardUnsigned(prevValue);
        guardUnsigned(value);
        guardNotNull(value);
        return guardUnsigned(prevValue | value);
    }

    public int manuallyMultiGuardedMethod(int prevValue, int value, Object notNull) {
        guardExclude1(guardExclude2(guardExclude3(prevValue)));
        guardExclude1(guardExclude2(guardExclude3(value)));
        guardNotNull(notNull);
        return guardExclude1(guardExclude2(guardExclude3(prevValue | value)));
    }

    @Unsigned
    public int guardedByAgentMethod(@Unsigned int prevValue, @Unsigned int value, @NotNull Object notNull) {
        return prevValue | value;
    }

    @Exclude1 @Exclude2 @Exclude3
    public int multiGuardedByAgentMethod(@Exclude1 @Exclude2 @Exclude3 int prevValue,
                                         @Exclude1 @Exclude2 @Exclude3 int value,
                                         @NotNull Object notNull)
    {
        return prevValue | value;
    }

    @Benchmark
    @OperationsPerInvocation(REPS)
    public int notGuarded(MyState state) {
        int result = 0;
        for( int i = 0; i < REPS; i++ ) {
            result = notGuardedMethod(result, i, "foo");
        }
        return result;
    }

    @Benchmark
    @OperationsPerInvocation(REPS)
    public int manuallyGuarded(MyState state) {
        int result = 0;
        for( int i = 0; i < REPS; i++ ) {
            result = manuallyGuardedMethod(result, i, "foo");
        }
        return result;
    }

    @Benchmark
    @OperationsPerInvocation(REPS)
    public int manuallyMultiGuarded(MyState state) {
        int result = 0;
        for( int i = 0; i < REPS; i++ ) {
            result = manuallyMultiGuardedMethod(result, i, "foo");
        }
        return result;
    }

    @Benchmark
    @OperationsPerInvocation(REPS)
    public int guardedByAgent(MyState state) {
        int result = 0;
        for( int i = 0; i < REPS; i++ ) {
            result = guardedByAgentMethod(result, i, "foo");
        }
        return result;
    }

    @Benchmark
    @OperationsPerInvocation(REPS)
    public int multiGuardedByAgent(MyState state) {
        int result = 0;
        for( int i = 0; i < REPS; i++ ) {
            result = multiGuardedByAgentMethod(result, i, "foo");
        }
        return result;
    }

    @SuppressWarnings("UnusedDeclaration")
    @State(Scope.Benchmark)
    public static class MyState {

        private final Stopwatch stopwatch = Stopwatch.createUnstarted();

        private String runName = System.getProperty("runName", "(unknownRunName)");
        private String runNo = System.getProperty("runNo", "(unknownRunNo)");
        private String countFile = System.getProperty("countFile");

        private int counter = -1;

        @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
        @Setup
        public void printStartInfo() throws IOException {
            if ( countFile != null ) {
                counter = Integer.parseInt(Files.toString(new File(countFile), Charset.defaultCharset()).trim());
                Files.write(String.valueOf(counter + 1), new File(countFile), Charset.defaultCharset());
            }
            System.out.printf("(In progress: Fork (%s).%d %s)%n", runNo, counter, runName);
            stopwatch.reset();
            stopwatch.start();
        }

        @TearDown
        public void printEndInfo() {
            stopwatch.stop();
            System.out.println("Single fork time: " + stopwatch + String.format(" ((%s).%d %s)", runNo, counter, runName));
        }

        @Setup
        public void sanityCheck() {
            // run some tests for sanity-checking the agent
            if ( GuardsAgent.getInstance().isInstalled() && !GuardsAgent.getInstance().getOptions().isXNopMode() ) {
                try {
                    sanityCheckMethod(1);
                    throw new RuntimeException("Expected ContractViolationError not thrown");
                }
                catch ( ContractViolationError e ) {
                    check(":return[-1]\n", e);
                }
                try {
                    sanityCheckMethod(-1);
                    throw new RuntimeException("Expected ContractViolationError not thrown");
                }
                catch ( ContractViolationError e ) {
                    check(":input[0]\n", e);
                }
                try {
                    multiSanityCheckMethod(-1);
                    throw new RuntimeException("Expected ContractViolationError not thrown");
                }
                catch ( ContractViolationError e ) {
                    check(Exclude1.class, e);
                }
                try {
                    multiSanityCheckMethod(-2);
                    throw new RuntimeException("Expected ContractViolationError not thrown");
                }
                catch ( ContractViolationError e ) {
                    check(Exclude2.class, e);
                }
                try {
                    multiSanityCheckMethod(-3);
                    throw new RuntimeException("Expected ContractViolationError not thrown");
                }
                catch ( ContractViolationError e ) {
                    check(Exclude3.class, e);
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

        private static void check(String contains, ContractViolationError e) {
            if ( !e.getMessage().contains(contains) ) {
                throw new RuntimeException("Thrown ContractViolationError did not meet expectations:\n'" + contains + "'\n" + e);
            }
            int pos = e.getMessage().indexOf('\n');
            System.out.println("Sanity check OK: " + e.getMessage().substring(0, pos));
        }

        private static void check(Class<? extends Annotation> type, ContractViolationError e) {
            check("\n  Guard : @" + type.getName() + "()\n", e);
        }
    }

}
