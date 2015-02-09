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

package ch.raffael.guards.agent;

import java.lang.reflect.Method;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
abstract class ParameterNames {

    private static final ParameterNames INSTANCE;
    static {
        ParameterNames instance = null;
        try {
            Class.forName("java.lang.reflect.Parameter");
            instance = new JDK8();
        }
        catch ( ClassNotFoundException e ) {
            // ignore
        }
        if ( instance == null ) {
            INSTANCE = new JDK7();
        }
        else {
            INSTANCE = instance;
        }
    }

    static String get(Method method, int index) {
        return INSTANCE.parameterName(method, index);
    }

    abstract String parameterName(Method method, int index);

    private static class JDK7 extends ParameterNames {
        @Override
        String parameterName(Method method, int index) {
            return "arg" + index;
        }
    }

    private static class JDK8 extends ParameterNames {
        @Override
        String parameterName(Method method, int index) {
            return method.getParameters()[index].getName();
        }
    }

}
