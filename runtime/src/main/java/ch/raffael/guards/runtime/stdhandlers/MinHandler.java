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

import ch.raffael.guards.Min;
import ch.raffael.guards.definition.Guard;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class MinHandler extends Guard.Handler<Min> {

    private final int minInt;
    private final long min;

    public MinHandler(Min annotation) {
        super(annotation);
        min = annotation.value();
        minInt = min > Integer.MIN_VALUE ? (int)min : Integer.MIN_VALUE;
    }

    public boolean test(int value) {
        return value >= minInt;
    }

    public boolean test(long value) {
        return value >= min;
    }

    public boolean test(float value) {
        return value >= min;
    }

    public boolean test(double value) {
        return value >= min;
    }

    public boolean test(BigInteger value) {
        return value.compareTo(BigInteger.valueOf(min)) >= 0;
    }

    public boolean test(BigDecimal value) {
        return value.compareTo(BigDecimal.valueOf(min)) >= 0;
    }
}