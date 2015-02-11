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

package ch.raffael.guards.runtime;

/**
 * Error thrown if a guard does not meet the contract.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class IllegalGuardError extends GuardNotApplicableError {

    public IllegalGuardError() {
    }

    public IllegalGuardError(String message) {
        super(message);
    }

    public IllegalGuardError(Throwable cause) {
        super(cause);
    }

    public IllegalGuardError(String message, Throwable cause) {
        super(message, cause);
    }
}