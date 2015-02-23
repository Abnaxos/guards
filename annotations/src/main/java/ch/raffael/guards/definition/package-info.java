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

/**
 * Contains the classes needed for guard definitions.
 *
 * Guards are defined using annotations that may be applied either on
 * {@link java.lang.annotation.ElementType#PARAMETER parameters} or
 * {@link java.lang.annotation.ElementType#METHOD methods.}. If a parameter is annotated,
 * the guard applies to the parameter, if the method is annotated, it applies to the
 * method's return value.
 *
 * The guards agent will instrument the code at class-loading time. It's recommended to
 * set the retention policy of guard annotations to
 * {@link java.lang.annotation.RetentionPolicy#CLASS CLASS}, so loading guarded
 * classes won't fail if the annotations are not present. Note, however, that the
 * annotation classes **must** be loadable by the respective class loaders of the annotated
 * classes in order for the agent to instrument the code. **If the guard annotations are
 * not available at runtime, the agent will silently not perform any instrumentation.**
 *
 * For more information on implementing guards, see
 * {@link ch.raffael.guards.definition.Guard @Guard}.
 *
 * Mappings
 * ========
 *
 * Guards also supports mapping alien annotations to guards. This is done using the
 * META-INF/ch.raffael.guards.mappings.properties file in the classpath. It contains
 * mappings like these in the standard properties format:
 *
 *     javax.annotation.Nonnull: ch.raffael.guards.NotNull
 *
 * The above example maps the `@Nonnull` annotations from the abandoned JSR 305 to our
 * own {@link ch.raffael.guards.NotNull} annotation, i.e. the agent will instrument the
 * code the same way.
 *
 * Mappings are provided for JSR 305 and the JetBrains annotations where applicable.
 *
 * @todo Provide a way to map annotation arguments when mapping guards.
 *
 * This could be done by mapping to a class that implements the target annotation and
 * takes the original annotation as sole constructor argument. Such a class could then
 * be used to map all the information. Example:
 *
 * ```java
 *
 * public @interface GuardToBeMapped {
 *     String foo();
 * }
 *
 * {@literal @}Guard(description="...")
 * public @interface TargetGuardAnnotation {
 *     String bar();
 * }
 *
 * public class MyMapping implements TargetGuardAnnotation {
 *     private final GuardToBeMapped mapped;
 *     public MyMapping(GuardToBeMapped mapped) {
 *         this.mapped = mapped;
 *     }
 *     public String bar() {
 *         return mapped.foo();
 *     }
 * }
 * ```
 *
 * ch.raffael.guards.mappings.properties:
 *
 *     GuardToBeMapped: MyMapping
 *
 * @see ch.raffael.guards.definition.Guard @Guard
 * @see ch.raffael.guards.definition.util
 */
package ch.raffael.guards.definition;
