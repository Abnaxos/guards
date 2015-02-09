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

package ch.raffael.guards.runtime.stdhandlers;

import ch.raffael.guards.Real;
import ch.raffael.guards.definition.FloatUnboxingHandler;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
public class RealHandler extends FloatUnboxingHandler<Real> {
    public RealHandler(Real annotation) {
        super(annotation);
    }
    @Override
    public boolean test(float value) {
        return value != Float.NaN
                && value != Float.POSITIVE_INFINITY
                && value != Float.NEGATIVE_INFINITY;
    }
    @Override
    public boolean test(double value) {
        return value != Double.NaN
                && value != Double.POSITIVE_INFINITY
                && value != Double.NEGATIVE_INFINITY;
    }
}
