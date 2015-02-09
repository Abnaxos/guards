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

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public enum GuardFlag {

    /**
     * Normally, `null` values will silently be accepted as valid. After all, we've got the
     * `NotNull` annotation to forbid `null` values (currently the only annotation that actually
     * uses this flag). Setting this flag enables the guard to test `null` values.
     */
    TEST_NULLS,

    /**
     * Specify that the guard tests the method entry based on some global state instead of a
     * parameter value. Some example ideas what this could be useful from:
     *
     *  *  `@Future`: Always throw an exception, the method is not implemented yet.
     *
     *  *  `@TestOnly`: The method is only callable from unit tests (e.g. by inspecting the stack
     *     trace, a system property or the presence of a class).
     *
     *  *  `@CallableFrom`: Test the caller of a method.
     *
     *  *  `@RequirePermission`: Require a `java.security.Permission` (though this is a dangerous
     *     idea because it would allow to turn off security by disabling the guard instrumentation
     *     using filtering or just not using the agent at all).
     */
    @Future
    METHOD_CALL_GUARD

}
