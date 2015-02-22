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
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Meta-annotation to define new guard annotations.
 * <p/>
 * Each guard annotation must provide a public static inner class named `Tests` that extends
 * {@link ch.raffael.guards.definition.Guard.Handler Guard.Test}. The type parameter `T` must be
 * the guard annotation itself (it may also be just {@link Annotation Annotation}). Alternatively,
 * the tests class may also be specified using the attribute {@link #handler()}.
 * <p/>
 * The test class must contain one ore more methods called `test` that take one argument and
 * return a `boolean`. These methods must return `true`, if the value meets the guard's requirements
 * or `false` otherwise. `null` values will not be tested unless the corresponding test method
 * is annotated with {@link CheckNulls @CheckNulls}. All tests must provide a public constructor
 * matching exactly {@link ch.raffael.guards.definition.Guard.Handler#Handler(java.lang.annotation.Annotation,
 * Class) Guard.Test(T, Guard.Type, Class<?>)}.
 * <p/>
 * The test method used for checks is derived from the declared compile-time type of the
 * annotated element (parameter type or method return type). The method with the **most specific**
 * type is chosen (e.g., if there's a check method for both `Iterable` and `Collection`, the test
 * for `Collection` is used for a `Set`).
 * <p/>
 * For primitive types, method matching follows the JVM rules. The following conversions will be
 * tried, the first matching test will be used:
 * <p/>
 * *  `byte` &rarr; `short` &rarr; `int` &rarr; `long` *  `char` &rarr; `int` &rarr; `long` *
 * `float` &rarr; `double`
 * <p/>
 * If the test method is ambiguous or no test matches, the agent will throw a {@link
 * ch.raffael.guards.GuardNotApplicableError GuardNotApplicableError}. **The agent will *not*
 * perform any auto-(un)boxing.**
 * <p/>
 * The message of the exception thrown if a guard is violated is specified in the {@link #message()
 * description}. It may also refer to arguments of the guard annotation using a format string as
 * specified by {@link String#format(String, Object...)} String.format())}. Arguments to be included
 * must be annotated with {@link Index @Index}, starting with 0.
 * <p/>
 * Guard annotations may also contain a boolean argument annotated with {@link Switch @Disabler}.
 * This may be used to disable or enable the actual guard checks. This is useful e.g. for {@link
 * ch.raffael.guards.NoNulls @NoNulls}, when you expect large collections and therefore a heavy
 * performance impact because of the check, but want to annotate the parameter/return value
 * nevertheless for documentation.
 * <p/>
 * Example =======
 * <p/>
 * ```java {@literal @}Target({ ElementType.METHOD, ElementType.PARAMETER }) {@literal
 * @}Retention(RetentionPolicy.CLASS) {@literal @}Documented {@literal @}Guard(description = "The
 * message if the guard is violated") public @interface MyGuard {
 * <p/>
 * class Test extends Guard.Test<MyGuard> { public Test(MyGuard annotation, Class<?> valueType) { super(annotation, type, valueType); } public boolean check(Object
 * value) { return value != null; } } } ```
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 * @todo Do perform auto-(un)boxing?
 * <p/>
 * This may be more problematic than it first sounds as it interferes with not checking `null`
 * values by default.
 * <p/>
 * Actually, I think performing (un)boxing automatically is a bad idea. Just consider the {@link
 * ch.raffael.guards.NotNull @NotNull} annotation: With auto-boxing, an `int` argument would be
 * boxed to Integer, and then checked for `null`. What we actually want is the guard not being
 * applicable to `int`.
 * <p/>
 * With unboxing, the problem is handling `null` values.
 * <p/>
 * We could consider introducing a annotation to specify boxing/unboxing behaviour, but at the end
 * of the day, just writing boxed/primitive variants isn't that much work and gives you the full
 * flexibility how to handle the values.
 * <p/>
 * We'll just provide some abstract classes for common use cases in {@link
 * ch.raffael.guards.definition.util}.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Guard {

    /**
     * The message format for the exception when the guard is violated as specified by {@link
     * java.util.Formatter Formatter}. Arguments to the format are specified by {@link Index @Index}
     * annotations on the respective annotation parameters.
     */
    String message() default "";

    /**
     * The handler class. If not specified (i.e. `Void.class`), the agent will look for a nested
     * class called `"Test`".
     */
    Class<? extends Handler<?>> handler() default ByConvention.class;

    PerformanceImpact performanceImpact();

    @GuardAnnotation
    Class<? extends Annotation>[] supersetOf() default {};

    @GuardAnnotation
    Class<? extends Annotation>[] subsetOf() default {};

    @GuardAnnotation
    Class<? extends Annotation>[] equalTo() default {};

    @GuardAnnotation
    Class<? extends Annotation[]>[] intersectingWith() default {};

    @GuardAnnotation
    Class<? extends Annotation>[] disjointFrom() default {};

    @GuardAnnotation
    Class<? extends Annotation>[] synonymousTo() default {};

    @GuardAnnotation
    Class<? extends Annotation>[] inconsistentWith() default {};

    RelationRule[] relations() default {};

    String[] validate() default {};

    GuardFlag[] flags() default {};

    OrderingTendency orderingTendency() default OrderingTendency.PRIMARY;
    Class<? extends Annotation>[] before() default {};
    Class<? extends Annotation>[] after() default {};


    /**
     * Base class for handlers. This is also the point where we could add nor features in the
     * future, like e.g. consistency checking, merging guard annotations with checking whether
     * a guard widens the contract of another guard etc.
     *
     * @param <T>
     */
    @GuardAnnotation
    abstract class Handler<T extends Annotation> {

        /**
         * The annotation, used to query annotation arguments.
         */
        protected final T annotation;

        /**
         * Constructor for tests. Each subclass must provide a matching constructor.
         *
         * @param annotation The annotation.
         * @param valueType  The compile-time type of the value to be checked.
         */
        protected Handler(T annotation) {
            this.annotation = annotation;
        }

        /**
         * Constructor for tests. Each subclass must provide a matching constructor.
         *
         * @param annotation The annotation.
         * @param valueType  The compile-time type of the value to be checked.
         */
        protected Handler() {
            this(null);
        }

        /**
         * Marks a method as test method. This may be useful to be able to use different tests
         * for different types with the same erasure, e.g. one test for `List<? extends Number>`
         * and another one for `List<? extends String>`. Because this would lead to two methods
         * named "`test`" that take a `List` as argument, you'll have to name those two arguments
         * differently. This allows the guards agent to still recognise these methods as test
         * methods.
         */
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        public @interface Test {
        }

        /**
         * Declare automatic type conversions. Possible type conversions are:
         *
         *  *  `byte` -> `short` -> `int` -> `long`
         *  *  `char` -> `int` -> `long`
         *  *  `float` -> `double`
         */
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Future
        public @interface ConvertFrom {

            /**
             * The primitive types to convert.
             */
            Class<?>[] value();

            /**
             * Enable unboxing. Not that you shouldn't use that when `checkNullValues` is `true`.
             * The conversion will simply throw a `NullPointerException` if trying to unbox `null`
             * values.
             */
            boolean unboxing();
        }

    }

    /**
     * Pseudo handler to lookup the handler by convention.
     */
    public final class ByConvention extends Guard.Handler<Annotation> {
        private ByConvention() {
            super(null);
        }
    }

    /**
     * Pseudo handler to mark that all values are valid for a guard.
     */
    public final class AlwaysTrue extends Guard.Handler<Annotation> {
        private AlwaysTrue() {
            super(null);
        }
        public static boolean alwaysTrue() {
            return true;
        }
    }

    /**
     * Configure the automatic type conversions that may be done implicitly. Use this annotation
     * on handler classes to add automatic conversions.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface TypeConversions {

        TypeConversions DEFAULTS = new TypeConversions() {
            @Override
            public boolean widen() {
                return true;
            }
            @Override
            public boolean widenToLong() {
                return false;
            }
            @Override
            public boolean widenToDouble() {
                return false;
            }
            @Override
            public boolean unbox() {
                return true;
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return TypeConversions.class;
            }
            @Override
            public int hashCode() {
                return memberHash("widen", widen())
                        + memberHash("widenToLong", widenToLong())
                        + memberHash("widenToDouble", widenToDouble())
                        + memberHash("unbox", unbox());
            }
            private int memberHash(String name, boolean value) {
                return (name.hashCode() * 127) ^ Boolean.valueOf(value).hashCode();
            }
            @Override
            public boolean equals(Object obj) {
                if ( !(obj instanceof TypeConversions) ) {
                    return false;
                }
                TypeConversions that = (TypeConversions)obj;
                return widen() == that.widen()
                        && widenToLong() == that.widenToLong()
                        && widenToDouble() == that.widenToDouble()
                        && unbox() == that.unbox();
            }
            @Override
            public String toString() {
                return "@" + TypeConversions.class.getName() + "("
                        + "widen=" + widen()
                        + ",widenToLong=" + widenToLong()
                        + ",widenToDouble=" + widenToDouble()
                        + ",unbox=" + unbox()
                        + ")";
            }
        };

        /**
         * Widen types within single word bounds. This enables the following conversions:
         *
         *  *  `byte`,`short`,`char` &rarr; `int`
         *  *  `byte` &rarr; `short`
         *
         *  **Enabled by default.**
         */
        boolean widen() default true;

        /**
         * Widen integer types also from single to double word bounds, i.e. to long. This is
         * disabled by default because this involves widening the values from one to two stack
         * entries, which may generally lead to more costly operations. While it's to be expected
         * that the performance impact of such a conversion is nearly zero, such conversions will
         * be done only if enabled explicitly.
         *
         * Conversions enabled by this flag:
         *
         *  *  `byte`,`short`,`char`,`int` &rarr; `long`
         *
         *  **Disabled by default.**
         */
        boolean widenToLong() default false;

        /**
         * Widen float values to double values. Because this involves doubling the stack entries
         * needed to store the values, it's disabled by default. See also explanation in
         * {@link #widenToLong()}.
         *
         * Conversions enabled by this flag:
         *
         *  *  `float` &rarr; `double`
         *
         *  **Disabled by default.**
         *
         * @see #widenToLong()
         */
        boolean widenToDouble() default false;

        /**
         * Enables implicit unboxing of boxed values. This should not be used for guards that also
         * check `null` values. Because guards will be checking `null` values only very rarely,
         * this options is enabled by default. The unboxed values are subject to further conversions
         * through the `widen*` flags.
         *
         * **Enabled by default.**
         */
        boolean unbox() default true;

    }

}
