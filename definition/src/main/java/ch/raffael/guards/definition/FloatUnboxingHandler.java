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
public abstract class FloatUnboxingHandler<T extends Annotation> extends Guard.Handler<T> {

    protected FloatUnboxingHandler(T annotation) {
        super(annotation);
    }

    public boolean test(Float value) {
        return test(value.floatValue());
    }

    public boolean test(Double value) {
        return test(value.doubleValue());
    }

    public boolean test(float value) {
        return test((double)value);
    }

    public abstract boolean test(double value);

}
