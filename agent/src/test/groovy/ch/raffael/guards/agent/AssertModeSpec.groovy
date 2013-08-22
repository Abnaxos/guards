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
/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class AssertModeSpec extends GuardsSpecification {

    def setup() {
        tf.transformer.mode = GuardsTransformer.Mode.ASSERT
    }

    def "AssertionError on invalid parameter"() {
      given:
        tf.assertions(true)
        def o = tf.create("ParameterCheck")

      when:
        o.invalid(null)

      then:
        def e = thrown AssertionError
        e.message.startsWith '@Invalid violation '
        e.message.contains '\nParameter obj: '
    }

    def "AssertionError on invalid return value"() {
      given:
        tf.assertions(true)
        def o = tf.create("ReturnCheck")

      when:
        o.invalid(null)

      then:
        def e = thrown AssertionError
        e.message.startsWith '@Invalid violation '
        e.message.contains '\nReturn value: '
    }

    def "No AssertionErrors if assertions disabled"() {
      given:
        tf.assertions(false)
        def p = tf.create("ParameterCheck")
        def r = tf.create("ReturnCheck")

      when:
        p.invalid(null)
        r.invalid(null)

      then:
        notThrown AssertionError
    }

}