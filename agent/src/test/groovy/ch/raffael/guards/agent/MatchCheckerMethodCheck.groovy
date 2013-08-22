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

import spock.lang.Specification

import java.awt.*
import java.util.List


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class MatchCheckerMethodCheck extends Specification {

    def "Also matches super types"() {
      given:
        def cls = new Object() {
            public boolean check(EventObject value) {
                return true
            }
        }.getClass()

      when:
        def list = CheckerBridge.filterCheckerMethods(AWTEvent.class, cls)

      then:
        list.size() == 1
    }

    def "Matches most specific type"() {
      given:
        def cls = new Object() {
            public boolean check(Iterable value) {
                return true
            }
            public boolean check(Collection value) {
                return true
            }
        }.getClass()

      when:
        def list = CheckerBridge.filterCheckerMethods(List.class, cls)

      then:
        list.size() == 1
        list.first.type == Collection
    }

    def "Doesn't match 'too' specific types"() {
      given:
        def cls = new Object() {
            public boolean check(Iterable value) {
                return true
            }
            public boolean check(Collection value) {
                return true
            }
        }.getClass()

      when:
        def list = CheckerBridge.filterCheckerMethods(Iterable.class, cls)

      then:
        list.size() == 1
        list.first.type == Iterable
    }

}