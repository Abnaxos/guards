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

package util;

import ch.raffael.guards.agent.guava.base.Stopwatch;
import ch.raffael.guards.agent.guava.base.Ticker;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Util {

    public static String formatNanos(final long nanos) {
        return Stopwatch.createUnstarted(new Ticker() {
            @Override
            public long read() {
                return nanos;
            }
        }).toString();
    }

}
