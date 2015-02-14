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

import ch.raffael.guards.Unsigned;
import ch.raffael.guards.definition.Guard;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
public class NotNegativeHandler extends Guard.Handler<Unsigned> {

    private static final BigDecimal DEC_ZERO = new BigDecimal(0);
    private static final BigInteger BIGINT_ZERO = new BigInteger("0");

    public NotNegativeHandler(Unsigned annotation) {
        super(annotation);
    }

    public boolean test(int value) {
        return value >= 0;
    }

    public boolean test(long value) {
        return value >= 0;
    }

    public boolean test(float value) {
        return value >= 0;
    }

    public boolean test(double value) {
        return value >= 0;
    }

    public boolean check(BigDecimal value) {
        return DEC_ZERO.compareTo(value) >= 0;
    }

    public boolean check(BigInteger value) {
        return BIGINT_ZERO.compareTo(value) >= 0;
    }
}
