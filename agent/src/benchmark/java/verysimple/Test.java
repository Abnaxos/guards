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

package verysimple;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import util.Util;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.guava.base.Stopwatch;
import ch.raffael.guards.runtime.ContractViolationError;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Test {

    public static void main(String[] args) throws Exception {
        //GuardsAgent.installAgent(args.length > 0 ? args[0] : "");
        //Thread.sleep(5000);
        try {
            LinkedList<Stopwatch> stopWatches = new LinkedList<>();
            for( int j = 0; j < 10; j++ ) {
                Stopwatch sw = Stopwatch.createStarted();
                for( int i = 0; i < 10000000; i++ ) {
                    println(test(null, "huiiii!"));
                    nop(null);
                    //Thread.sleep(1000);
                    println(new Test(42));
                    println(new Test(new Object()));
                }
                System.out.println(sw.stop());
                stopWatches.add(sw);
            }
            long time = 0;
            for( Stopwatch sw : stopWatches ) {
                time += sw.elapsed(TimeUnit.NANOSECONDS);
            }
            time /= stopWatches.size();
            System.out.println("=> " + Util.formatNanos(time));
        }
        catch ( ContractViolationError e ) {
            e.printStackTrace();
        }
    }

    public Test(int x) {

    }

    public Test(@NotNull Object foo) {

    }


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

    public static Object nop(Object unused) {
        return null;
    }

    public static void println(Object msg) {
        //System.out.println(msg);
    }

}
