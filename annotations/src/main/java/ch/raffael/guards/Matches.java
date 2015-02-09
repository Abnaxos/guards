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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.MagicConstant;

import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.definition.PerformanceImpact;


/**
 * Checks that a {@link CharSequence} matches the specified regular expression.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Guard(message = "Value must match $value",
        performanceImpact = PerformanceImpact.HIGH)
public @interface Matches {

    /**
     * The regular expression.
     */
    @Language("RegExp")
    String value();

    /**
     * If `true`, use {@link java.util.regex.Matcher#find() find()} instead of {@link
     * java.util.regex.Matcher#matches()} () matches()}.
     */
    boolean find() default false;

    /**
     * Flags for {@link Pattern#compile(String, int) Pattern.compile()}.
     */
    @MagicConstant(flagsFromClass = Pattern.class)
    int flags() default 0;

}
