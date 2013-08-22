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

package ch.raffael.guards.test.transform;

import ch.raffael.guards.test.guards.Equals;
import ch.raffael.guards.test.guards.Invalid;
import ch.raffael.guards.test.guards.InvalidNoNulls;
import ch.raffael.guards.test.guards.NotZero;
import ch.raffael.guards.test.guards.Valid;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ParameterCheck {

    public void invalid(@Invalid Object obj) {
    }

    public void invalidNoNulls(@InvalidNoNulls Object obj) {
    }

    public void valid(@Valid Object obj) {
    }

    public void equalsFoo(@Equals("foo") String str) {

    }

    public void intNotZero(@NotZero int val) {

    }

    public void longNotZero(@NotZero long val) {

    }

    public ParameterCheck() {
    }

}
