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
import java.util.logging.Level;

import ch.raffael.guards.Nullable;

import static ch.raffael.guards.agent.Logging.LOG;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
abstract class  ParameterNames {

    private static final ParameterNames INSTANCE;
    static {
        ParameterNames instance = null;
        try {
            Class.forName("java.lang.reflect.Parameter");
            instance = new JDK8().usable();
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

    /**
     * @todo Looks like we'll have to use JDK8 at runtime. If this is the case, remove reflection.
     */
    private static class JDK8 extends ParameterNames {

        private static final Method GET_PARAMETERS;
        private static final Method GET_NAME;
        static {
            Method getParameters = null;
            Method getName = null;
            try {
                getParameters = Method.class.getMethod("getParameters");
                getName = Class.forName("java.lang.reflect.Parameter").getMethod("getName");
            }
            catch ( Exception e ) {
                LOG.log(Level.WARNING, "Unexpected reflection error preparing parameter names", e);
            }
            GET_PARAMETERS = getParameters;
            GET_NAME = getName;
        }

        @Nullable
        JDK8 usable() {
            if ( GET_PARAMETERS != null && GET_NAME != null ) {
                return this;
            }
            else {
                return null;
            }
        }

        @Override
        String parameterName(Method method, int index) {
            try {
                Object[] parameters = (Object[])GET_PARAMETERS.invoke(method);
                return (String)GET_NAME.invoke(parameters[index]);
            }
            catch ( Exception e ) {
                LOG.log(Level.WARNING, "Unexpected reflection error getting parameter name", e);
                return "arg" + index;
            }
        }
    }

}
