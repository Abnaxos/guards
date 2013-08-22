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

package ch.raffael.guards.definition;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Meta-annotation to define new guard annotations.
 *
 * Each guard annotation must provide a public static inner class named `Checker` that
 * extends {@link Checker Guard.Checker}. The type parameter `T` should be the guard
 * annotation itself (it may also be just {@link Annotation Annotation}).
 *
 * The checker class must contain one ore more methods called `check` that take one
 * argument and return a `boolean`. These methods must return `true`, if the value meets
 * the guard's requirements or `false` otherwise. `null` values will not be checked
 * unless the corresponding checker method is annotated with
 * {@link CheckNulls @CheckNulls}. All checkers must provide a public constructor matching
 * exactly
 * {@link Guard.Checker#Checker(java.lang.annotation.Annotation, ch.raffael.guards.definition.Guard.Type, Class)
 * Guard.Checker(T, Guard.Type, Class<?>)}.
 *
 * The checker method used for checks is derived from the declared compile-time type of
 * the annotated element (parameter type or method return type). The method with the
 * **most specific** type is chosen (e.g., if there's a check method for both `Iterable`
 * and `Collection`, the checker for `Collection` is used for a `Set`).
 *
 * For primitive types, method matching follows the JVM rules. The following conversions
 * will be tried, the first matching checker will be used:
 *
 *  *  `byte` &rarr; `short` &rarr; `int` &rarr; `long`
 *  *  `char` &rarr; `int` &rarr; `long`
 *  *  `float` &rarr; `double`
 *
 * If the checker method is ambiguous or no checker matches, the agent will throw a
 * {@link ch.raffael.guards.GuardNotApplicableError GuardNotApplicableError}. **The agent
 * will *not* perform any auto-(un)boxing.**
 *
 * The message of the exception thrown if a guard is violated is specified in the
 * {@link #message() description}. It may also refer to arguments of the guard
 * annotation using a format string as specified by
 * {@link String#format(String, Object...)} String.format())}. Arguments to be included
 * must be annotated with {@link Index @Index}, starting with 0.
 *
 * Guard annotations may also contain a boolean argument annotated with
 * {@link Disabler @Disabler}. This may be used to disable or enable the actual guard
 * checks. This is useful e.g. for {@link ch.raffael.guards.NoNulls @NoNulls}, when you
 * expect large collections and therefore a heavy performance impact because of the check,
 * but want to annotate the parameter/return value nevertheless for documentation.
 *
 * Example
 * =======
 *
 * ```java
 * {@literal @}Target({ ElementType.METHOD, ElementType.PARAMETER })
 * {@literal @}Retention(RetentionPolicy.CLASS)
 * {@literal @}Documented
 * {@literal @}Guard(description = "The message if the guard is violated")
 * public @interface MyGuard {
 *
 *     class Checker extends Guard.Checker<MyGuard> {
 *         public Checker(MyGuard annotation, Guard.Type type, Class<?> valueType) {
 *             super(annotation, type, valueType);
 *         }
 *         public boolean check(Object value) {
 *             return value != null;
 *         }
 *     }
 * }
 * ```
 *
 * @todo Do perform auto-(un)boxing?
 *
 * This may be more problematic than it first sounds as it interferes with not checking
 * `null` values by default.
 *
 * Actually, I think performing (un)boxing automatically is a bad idea. Just consider the
 * {@link ch.raffael.guards.NotNull @NotNull} annotation: With auto-boxing, an `int`
 * argument would be boxed to Integer, and then checked for `null`. What we actually want
 * is the guard not being applicable to `int`.
 *
 * With unboxing, the problem is handling `null` values.
 *
 * We could consider introducing a annotation to specify boxing/unboxing behaviour, but
 * at the end of the day, just writing boxed/primitive variants isn't that much work and
 * gives you the full flexibility how to handle the values.
 *
 * We'll just provide some abstract classes for commit use cases in
 * {@link ch.raffael.guards.definition.util}
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Guard {

    /**
     * The message format for the exception when the guard is violated as specified by
     * {@link java.util.Formatter Formatter}. Arguments to the format are specified by
     * {@link Index @Index} annotations on the respective annotation parameters.
     */
    String message();

    /**
     * The types the guard may be applied to.
     */
    Type[] types() default { Type.PARAMETER, Type.RESULT };

    /**
     * Base class for checkers.
     * @param <T>
     */
    abstract class Checker<T extends Annotation> {

        /**
         * The annotation, used to query annotation arguments.
         */
        protected final T annotation;
        /**
         * The element type that was annotated (method or parameter).
         */
        protected final Type checkType;
        /**
         * The (compile-time) type of the annotated element.
         */
        protected final Class<?> valueType;

        /**
         * Constructor for checkers. Each subclass must provide a matching constructor.
         *
         * @param annotation    The annotation.
         * @param checkType     The type of the check (parameter or return value).
         * @param valueType     The compile-time type of the value to be checked.
         */
        protected Checker(T annotation, Type checkType, Class<?> valueType) {
            this.annotation = annotation;
            this.checkType = checkType;
            this.valueType = valueType;
        }

    }

    /**
     * The type of a guard.
     */
    enum Type {
        /**
         * Guards a parameter.
         */
        PARAMETER,
        /**
         * Guards a method's return value.
         */
        RESULT
    }

}
