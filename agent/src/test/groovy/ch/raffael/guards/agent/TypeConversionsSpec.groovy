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





package ch.raffael.guards.agent

import ch.raffael.guards.GuardNotApplicableError


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class TypeConversionsSpec extends AgentSpecification {

    def "Standard primitive type conversions are supported (exception integers to floats)"() {
      given:
        guards {
            guard.test(long)
            guard.test(double)
            method('byteArg').param(byte).guard()
            method('shortArg').param(short).guard()
            method('intArg').param(int).guard()
            method('charArg').param(char).guard()
            method('floatArg').param(float).guard()
        }

      when:
        guards.invoke {
            byteArg((byte)23)
            shortArg((short)666)
            intArg(424242)
            charArg((char)'A')
            floatArg((float)1f)
        }

      then:
        with(guardInvocations) {
            1 * invocation('byteArg', _, long.tt, 23)
            1 * invocation('shortArg', _, long.tt, 666)
            1 * invocation('intArg', _, long.tt, 424242)
            1 * invocation('charArg', _, long.tt, 65)
            1 * invocation('floatArg', _, double.tt, 1)
        }
        0 * _
    }

    def "Null values will always be valid if testNulls==false (default); test method won't be called"() {
      given:
        guards {
            guard.test(Object)

            method('foo').param(Object).guard()
        }

      when:
        guards.invoke {
            foo 'something'
            foo null
            foo 'some more'
        }

      then:
        with(guardInvocations) {
            1 * invocation(_, _, Object.tt, 'something')
            1 * invocation(_, _, Object.tt, 'some more')
        }
        0 * _
    }

    def "Null values will be tested if testNulls==true"() {
      given:
        guards {
            guard.test(Object)
            guard.testNulls = true

            method('foo').param(Object).guard()
        }

      when:
        guards.invoke {
            foo('something')
            foo(null)
            foo('some more')
        }

      then:
        with(guardInvocations) {
            1 * invocation(_, _, Object.tt, 'something')
            1 * invocation(_, _, Object.tt, null)
            1 * invocation(_, _, Object.tt, 'some more')
        }
        0 * _
    }

    def "Values will be unboxed and converted if testNulls==false"() {
      given:
        guards {
            guard.test(int)
            guard.test(double)

            method('shortArg').param(Short).guard()
            method('intArg').param(Integer).guard()
            method('floatArg').param(Float).guard()
        }

      when:
        guards.invoke {
            shortArg 23 as short
            shortArg null
            intArg 424242
            intArg null
            floatArg 1f
            floatArg null
        }

      then:
        with(guardInvocations) {
            1 * invocation('shortArg', _, int.tt, 23)
            1 * invocation('intArg', _, int.tt, 424242)
            1 * invocation('floatArg', _, double.tt, 1.0)
        }
        0 * _
    }

    def "Unboxing is disabled if testNulls==true"() {
      given:
        guards {
            guard.test(int)
            guard.testNulls = true

            method('shortArg').param(Short).guard()
            method('intArg').param(Integer).guard()
        }

      when:
        guards.invoke {
            invokeMethod(methodName, [null])
        }

      then:
        def e = thrown(BootstrapMethodError)
        e.cause instanceof GuardNotApplicableError
        e.cause.message.contains "$DynaGuards.METHODS_CLASSNAME.$methodName($argType.name)"
        e.cause.message.contains ': No matching test method found '

      where:
        methodName | argType
        'shortArg' | Short
        'intArg'   | Integer
    }

// What was I thinking? We do not autobox any values for now
// todo: might there be any use-cases where unboxing actually makes sense?
//    def "Autoboxing is supported but won't widen types"() {
//      given:
//        guards {
//            guard.test(Integer)
//
//            method('intArg').param(int).guard()
//            method('shortArg').param(short).guard()
//        }
//
//      when:
//        guards.invoke {
//            invokeMethod('intArg', 42)
//            invokeMethod('shortArg', (short)42)
//        }
//
//      then: "intArg() was properly guarded"
//        with(guardInvocations) {
//            1 * invocation('intArg', _, Integer.tt, 42)
//        }
//      then: "guarding of shortArg() failed"
//        def e = thrown(BootstrapMethodError)
//        e.cause instanceof GuardNotApplicableError
//      then: "no other invokations occurred"
//        0 * _._
//    }

}
