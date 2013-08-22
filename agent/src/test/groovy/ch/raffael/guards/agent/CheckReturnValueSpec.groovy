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
class CheckReturnValueSpec extends GuardsSpecification {

    def "@Invalid guard throws exception"() {
      given:
        def o = tf.create("ReturnCheck")

      when:
        o.invalid(new Object())

      then:
        def e = thrown IllegalStateException
        e.message.contains "Return value: The given value is always invalid\n"
        e.message.contains "\nValue: java.lang.Object@"
    }

    def "@Invalid guard throws exception with null"() {
      given:
        def o = tf.create("ReturnCheck")

      when:
        o.invalid(null)

      then:
        def e = thrown IllegalStateException
        e.message.contains "Return value: The given value is always invalid\n"
        e.message.contains "\nValue: null"
    }

    def "@InvalidNoNulls guard throws exception"() {
      given:
        def o = tf.create("ReturnCheck")

      when:
        o.invalidNoNulls(new Object())

      then:
        def e = thrown IllegalStateException
        e.message.contains "Return value: The given value is always invalid (except null)\n"
        e.message.contains "\nValue: java.lang.Object@"
    }

    def "@InvalidNoNulls guard does not exception with null"() {
      given:
        def o = tf.create("ReturnCheck")

      when:
        o.invalidNoNulls(null)

      then:
        notThrown IllegalStateException
    }

    def "@Equals throws exception if argument doesn't match"() {
      given:
        def o = tf.create("ReturnCheck")

      when:
        o.equalsFoo('bar')

      then:
        def e = thrown IllegalStateException
        e.message.endsWith 'Return value: Value must equal foo\nValue: bar'
    }

    def "@Equals doesn't throw exception if arguments match"() {
      given:
        def o = tf.create("ReturnCheck")

      when:
        o.equalsFoo('foo')

      then:
        notThrown IllegalStateException
    }

    def "@NotZero throws exception if int argument is zero"() {
      given:
        def o = tf.create("ReturnCheck")

      when:
        o.intNotZero(0)

      then:
        def e = thrown IllegalStateException
        e.message.contains "Return value: The value must not be zero\n"
        e.message.contains "\nValue: 0"
    }

    def "@NotZero throws exception if long argument is zero"() {
      given:
        def o = tf.create("ReturnCheck")

      when:
        o.longNotZero(0)

      then:
        def e = thrown IllegalStateException
        e.message.contains "Return value: The value must not be zero\n"
        e.message.contains "\nValue: 0"
    }

}