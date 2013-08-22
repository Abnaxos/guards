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

package ch.raffael.guards.definition.util;

import java.lang.annotation.Annotation;

import ch.raffael.guards.definition.Guard;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class NumberUnboxingChecker<T extends Annotation> extends Guard.Checker<T> {

    protected NumberUnboxingChecker(T annotation, Guard.Type checkType, Class<?> valueType) {
        super(annotation, checkType, valueType);
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
