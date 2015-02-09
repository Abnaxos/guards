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
public abstract class IntUnboxingHandler<T extends Annotation> extends Guard.Handler<T> {

    protected IntUnboxingHandler(T annotation) {
        super(annotation);
    }

    public boolean test(Byte value) {
        return test(value.intValue());
    }

    public boolean test(Short value) {
        return test(value.intValue());
    }

    public boolean test(Integer value) {
        return test(value.intValue());
    }

    public boolean test(Long value) {
        return test(value.intValue());
    }

    public boolean test(Character value) {
        return test((int)value.charValue());
    }
    public boolean test(int value) {
        return test((long)value);
    }

    public abstract boolean test(long value);

}
