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

import benchmarks.GuardBenchmark
import ch.raffael.guards.agent.guava.base.Stopwatch
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder

import java.util.concurrent.TimeUnit


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class RunGuardBenchmarks {

    private final String javaExecutable
    private final String agentPath
    private final String label

    RunGuardBenchmarks(String javaExecutable, String agentPath, String label) {
        this.javaExecutable = javaExecutable
        this.agentPath = agentPath
        this.label = label
    }

    static void main(String... cmdLineArgs) {
        new RunGuardBenchmarks(cmdLineArgs[0], cmdLineArgs[1], cmdLineArgs[2]).runBenchmarks()
    }

    private void runBenchmarks() {
        println "Java executable: $javaExecutable"
        println "Agent path: $agentPath"

        def jvmArgs = [
                '-DnoAgent=noAgent',
                agent('+XnopMode', 'XnopMethod=mh_constant'),
                agent('+XnopMode', 'XnopMethod=dedicated_method'),
                agent('+XnopMode', '+XinstrumentAll', 'XnopMethod=mh_constant'),
                agent('+XnopMode', '+XinstrumentAll', 'XnopMethod=dedicated_method'),

                [['-XinstrumentAll', '+XinstrumentAll'],
                 ['-XmutableCallSites', '+XmutableCallSites'],
                ].combinations().collect({ List args -> agent(args) }),
        ].flatten()

        for ( args in jvmArgs ) {
            println "Found benchmark invocation: $args"
        }
        println "Found ${jvmArgs.size()} benchmark invocations to run"

        int count = 0
        def countFile = File.createTempFile('guards-benchmark', '.counter')
        countFile.deleteOnExit()
        Stopwatch totalSW = Stopwatch.createStarted()
        jvmArgs.each { args ->
            countFile.write('0')
            def name = "${args.substring(args.indexOf('=') + 1)}"
            if ( !name ) {
                name = 'defaults'
            }
            OptionsBuilder ob = new OptionsBuilder().with {
                include GuardBenchmark.class.name

                mode Mode.Throughput

                warmupForks 1
                forks 1
                measurementIterations 20
                warmupIterations 10
                timeUnit TimeUnit.NANOSECONDS

                jvm javaExecutable
                jvmArgsAppend args, "-DrunName=$name", "-DrunNo=$label:$count/${jvmArgs.size()}", "-DcountFile=${countFile.toString()}"
                shouldFailOnError true

                result String.format("result-%03d--%s.txt", count, name)
                resultFormat ResultFormatType.TEXT
                output String.format("log-%03d--%s.txt", count, name)

                return it
            }
            println String.format('Running %s #%03d: %s', label, count, name)
            Stopwatch sw = Stopwatch.createStarted()
            new Runner(ob.build()).run()
            sw.stop()
            println "Time: $sw"
            count++
        }
        println "Total time: $totalSW"
        countFile.delete()
    }

    private String agent(List args) {
        "-javaagent:$agentPath=${args.join(',')}" as String
    }

    private String agent(String... args) {
        agent(args as List)
    }

}
