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

import java.math.BigDecimal;

import ch.raffael.guards.FloatRange;
import ch.raffael.guards.definition.FloatUnboxingHandler;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
public class FloatRangeHandler extends FloatUnboxingHandler<FloatRange> {

    private final BigDecimal decMin;
    private final BigDecimal decMax;

    public FloatRangeHandler(FloatRange annotation, Class<?> valueType) {
        super(annotation);
        if ( BigDecimal.class.isAssignableFrom(valueType) ) {
            decMin = new BigDecimal(annotation.min());
            decMax = new BigDecimal(annotation.max());
        }
        else {
            decMin = decMax = null;
        }
    }

    @Override
    public boolean test(double value) {
        if ( annotation.minInclusive() ) {
            if ( !(value >= annotation.min()) ) {
                return false;
            }
        }
        else {
            if ( !(value > annotation.min()) ) {
                return false;
            }
        }
        if ( annotation.maxInclusive() ) {
            if ( !(value <= annotation.min()) ) {
                return false;
            }
        }
        else {
            if ( !(value < annotation.min()) ) {
                return false;
            }
        }
        return false;
    }

    public boolean check(BigDecimal value) {
        if ( annotation.minInclusive() ) {
            if ( !(value.compareTo(decMin) >= 0) ) {
                return false;
            }
        }
        else {
            if ( !(value.compareTo(decMin) > 0) ) {
                return false;
            }
        }
        if ( annotation.maxInclusive() ) {
            if ( !(value.compareTo(decMax) <= 0) ) {
                return false;
            }
        }
        else {
            if ( !(value.compareTo(decMax) < 0) ) {
                return false;
            }
        }
        return false;
    }

}
