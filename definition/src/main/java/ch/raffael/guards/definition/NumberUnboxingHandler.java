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

package ch.raffael.guards.definition;

import java.lang.annotation.Annotation;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class NumberUnboxingHandler<T extends Annotation> extends Guard.Handler<T> {

    protected NumberUnboxingHandler(T annotation) {
        super(annotation);
    }

    public boolean check(Byte value) {
        return check(value.intValue());
    }

    public boolean check(Short value) {
        return check(value.intValue());
    }

    public boolean check(Integer value) {
        return check(value.intValue());
    }

    public boolean check(Long value) {
        return check(value.intValue());
    }

    public boolean check(Character value) {
        return check((int)value.charValue());
    }

    public boolean check(int value) {
        return check((long)value);
    }

    public abstract boolean check(long value);

    public boolean check(Float value) {
        return check(value.floatValue());
    }

    public boolean check(Double value) {
        return check(value.doubleValue());
    }

    public boolean check(float value) {
        return check((double)value);
    }

    public abstract boolean check(double value);

}
