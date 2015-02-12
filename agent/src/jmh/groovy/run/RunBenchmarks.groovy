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







package run

import benchmarks.BasicBenchmark
import ch.raffael.guards.agent.guava.base.Stopwatch
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder

import java.util.concurrent.TimeUnit


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class RunBenchmarks {

    static final AGENT_ARGS = [
            ['-nopMode'],
            ['-instrumentAll', '+instrumentAll'],
//            ['multiGuardMethod=mh_guard', 'multiGuardMethod=invoker'],
            ['-mutableCallSites', '+mutableCallSites'],
    ]

    static void main(String... cmdLineArgs) {
        def javaExecutable = cmdLineArgs[0]
        def agentPath = cmdLineArgs[1]

        println "Java executable: $javaExecutable"
        println "Agent path: $agentPath"

        def jvmArgs = [
                '-DnoAgent=noAgent',
                agent(agentPath, ['+nopMode', 'nopMethod=mh_constant']),
                agent(agentPath, ['+nopMode', 'nopMethod=dedicated_method']),
                agent(agentPath, ['+nopMode', '+instrumentAll', 'nopMethod=mh_constant']),
                agent(agentPath, ['+nopMode', '+instrumentAll', 'nopMethod=dedicated_method']),
        ]
        jvmArgs.addAll(AGENT_ARGS.combinations().collect({ List args ->
            agent(agentPath, args)
        }))

        println "Found ${jvmArgs.size()} Benchmarks to run"

        int count = 0
        Stopwatch totalSW = Stopwatch.createStarted()
        jvmArgs.each { args ->
            def name = "${args.substring(args.indexOf('=') + 1)}"
            OptionsBuilder ob = new OptionsBuilder().with {
                include BasicBenchmark.class.name

                mode Mode.Throughput

                warmupForks 1
                forks 1
                measurementIterations 20
                warmupIterations 10
                timeUnit TimeUnit.NANOSECONDS

                param 'reps', '100000'

                jvm javaExecutable
                jvmArgsAppend args //, '-verbose:class'
                shouldFailOnError true

                result String.format("result-%03d--%s.txt", count, name)
                resultFormat ResultFormatType.TEXT
                output String.format("log-%03d--%s.txt", count, name)

                return it
            }
            println String.format('Running #%03d: %s', count, name)
            Stopwatch sw = Stopwatch.createStarted()
            new Runner(ob.build()).run()
            sw.stop()
            println "Time: $sw"
            count++
        }
        println "Total time: $totalSW"
    }

    private static String agent(String agentPath, List args) {
        "-javaagent:$agentPath=${args.join(',')}" as String
    }

}
