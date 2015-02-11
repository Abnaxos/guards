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
import java.math.BigInteger;

import ch.raffael.guards.IntRange;
import ch.raffael.guards.definition.IntUnboxingHandler;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
public class IntRangeHandler extends IntUnboxingHandler<IntRange> {
    private final int intMin;
    private final int intMax;
    private final BigInteger bigintMin;
    private final BigInteger bigintMax;
    private final BigDecimal bigdecMin;
    private final BigDecimal bigdecMax;
    @SuppressWarnings("ObjectEquality")
    public IntRangeHandler(IntRange annotation, Class<?> valueType) {
        super(annotation);
        if ( valueType == BigInteger.class ) {
            bigintMin = BigInteger.valueOf(annotation.min());
            bigintMax = BigInteger.valueOf(annotation.max());
            bigdecMin = null;
            bigdecMax = null;
        }
        else if ( valueType == BigDecimal.class ) {
            bigintMin = null;
            bigintMax = null;
            bigdecMin = new BigDecimal(annotation.min());
            bigdecMax = new BigDecimal(annotation.max());
        }
        else {
            bigintMin = null;
            bigintMax = null;
            bigdecMin = null;
            bigdecMax = null;
        }
        intMin = toInt(annotation.min());
        intMax = toInt(annotation.max());
    }
    private static int toInt(long value) {
        if ( value > Integer.MAX_VALUE ) {
            return Integer.MAX_VALUE;
        }
        else if ( value < Integer.MIN_VALUE ) {
            return Integer.MIN_VALUE;
        }
        else {
            return (int)value;
        }
    }
    public boolean test(int value) {
        return value >= intMin && value <= intMax;
    }
    public boolean test(long value) {
        return value >= annotation.min() && value <= annotation.max();
    }
    public boolean check(BigInteger value) {
        return value.compareTo(bigintMin) >= 0 && value.compareTo(bigintMax) <= 0;
    }
    public boolean check(BigDecimal value) {
        return value.compareTo(bigdecMin) >= 0 && value.compareTo(bigdecMax) <= 0;
    }
}