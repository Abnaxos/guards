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

import ch.raffael.guards.test.annotations.*
import spock.lang.Specification

import java.lang.annotation.AnnotationFormatError

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class AnnotationImplSpec extends Specification {

    def ClassSynthesizer synth = ClassSynthesizer.get(AnnotationImplSpec)

    def "Simple Annotation"() {
      when:
        def a = synth.implementAnnotation(Simple, [:])

      then:
        notThrown Throwable
    }

    def "Annotation with int parameter"() {
      when:
        def a = synth.implementAnnotation(IntParam, [value: 42 as int])

      then:
        a.value() == 42
    }

    def "Annotation with long parameter"() {
      when:
        def a = synth.implementAnnotation(LongParam, [foo: 42 as long])

      then:
        a.foo() == 42 as long
    }

    def "Annotation with int parameter (default value)"() {
      when:
        def a = synth.implementAnnotation(IntDefault, [:])

      then:
        a.foo() == 42 as int
    }

    def "Annotation with long parameter (default value)"() {
      when:
        def a = synth.implementAnnotation(LongDefault, [:])

      then:
        a.foo() == 42 as long
    }

    def "Annotation with int parameter (default value overridden)"() {
      when:
        def a = synth.implementAnnotation(IntDefault, [foo: 23 as int])

      then:
        a.foo() == 23 as int
    }

    def "Annotation with long parameter (default value overridden)"() {
      when:
        def a = synth.implementAnnotation(LongDefault, [foo: 23 as long])

      then:
        a.foo() == 23 as long
    }

    def "Annotation with array parameters"() {
      given:
        def ints = [1 as int, 2 as int, 3 as int] as int[]
        def longs = [4 as long, 5 as long, 6 as long] as long[]
        def classes = [Object.class, String.class, Integer.class] as Class[]

      when:
        def a = synth.implementAnnotation(ArrayParams, [
                intArray: ints,
                longArray: longs,
                classArray: classes
        ])

      then:
        !a.intArray().is(ints)
        a.intArray() == ints
        !a.longArray().is(longs)
        a.longArray() == longs
        !a.classArray().is(classes)
        a.classArray() == classes
    }

    def "AnnotationFormatError on superfluous arguments"() {
      when:
        def a = synth.implementAnnotation(IntParam, [value: 42 as int, error: 42 as int])

      then:
        def e = thrown AnnotationFormatError
        e.message.contains 'Unknown values specified: '
    }

    def "AnnotationFormatError missing arguments"() {
      when:
        def a = synth.implementAnnotation(IntParam, [:])

      then:
        def e = thrown AnnotationFormatError
        e.message.contains 'No value specified for '
    }

}