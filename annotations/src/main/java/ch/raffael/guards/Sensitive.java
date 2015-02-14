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

package ch.raffael.guards;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {

    final String SENSITIVE_MSG = "(concealed for security)";

    @SuppressWarnings("ClassExplicitlyAnnotation")
    public static class SensitiveImpl implements Sensitive {
        private static final SensitiveImpl INSTANCE = new SensitiveImpl();
        public static SensitiveImpl getInstance() {
            return INSTANCE;
        }
        @Override
        public Class<? extends Annotation> annotationType() {
            return Sensitive.class;
        }
        @Override
        public int hashCode() {
            return 0;
        }
        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof Sensitive;
        }
        @Override
        public String toString() {
            return "@" + Sensitive.class.getName() + "()";
        }
    }

}
