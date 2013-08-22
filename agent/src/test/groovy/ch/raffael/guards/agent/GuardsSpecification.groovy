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



package ch.raffael.guards.agent

import ch.raffael.guards.agent.testaux.TestLogHandler
import ch.raffael.guards.agent.testaux.TransformingClassLoader
import spock.lang.Specification

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.Logger


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
abstract class GuardsSpecification extends Specification {

    static {
        // setup logging
        def agentLogger = Logger.getLogger("ch.raffael.guards.agent")
        agentLogger.setLevel(Level.ALL);
        agentLogger.addHandler(new TestLogHandler())
        for ( Handler handler : Logger.getLogger("").getHandlers() ) {
            handler.setLevel(Level.ALL);
        }
    }

    protected final TransformingClassLoader tf = new TransformingClassLoader()

}