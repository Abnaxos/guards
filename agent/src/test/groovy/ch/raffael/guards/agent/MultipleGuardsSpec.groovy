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

import ch.raffael.guards.test.guards.Trace

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class MultipleGuardsSpec extends GuardsSpecification {

    Trace.Tracer tracer = Mock()
    def object = tf.create('MultipleGuards')

    def setup() {
        Trace.Checker.tracer(tracer)
    }

    def "Object parameter"() {
      given:
        def value = new Object()

      when:
        def ret = object.trace(value)

      then:
        ret == value
        2 * tracer.check(Object.class, value)
        0 * tracer._
    }

    def "Long parameter"() {
      given:
        def value = 42L

      when:
        def ret = object.traceLong(value)

      then:
        ret == value
        2 * tracer.check(long.class, value)
        0 * tracer._
    }

    def "Object return value"() {
      given:
        def value = new Object()

      when:
        def ret = object.traceReturn(value)

      then:
        ret == value
        2 * tracer.check(Object.class, value)
        0 * tracer._
    }

    def "Long return value"() {
      given:
        def value = 42L

      when:
        def ret = object.traceLongReturn(value)

      then:
        ret == value
        2 * tracer.check(long.class, value)
        0 * tracer._
    }

}