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

import ch.raffael.guards.agent.DynaGuards.Recorder
import ch.raffael.guards.agent.guava.reflect.TypeToken
import spock.lang.Specification
import spock.util.mop.Use

import static groovy.lang.Closure.DELEGATE_FIRST


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Use([ClassToTypeToken, TypeTokenToTypeToken])
abstract class AgentSpecification extends Specification {

    protected DynaGuards guards = null

    def setupSpec() {
        if ( !GuardsAgent.getInstance().installed ) {
            GuardsAgent.installAgent(null)
        }
    }

    protected guards(@DelegatesTo(value = DynaGuards, strategy = DELEGATE_FIRST) Closure config) {
        if ( guards == null ) {
            guards = new DynaGuards(Mock(Recorder))
        }
        DynaGuards.with(guards, config)
        return guards
    }

    static TypeToken type(Class type) {
        TypeToken.of(type)
    }

    static TypeToken type(TypeToken type) {
        type
    }

    protected Recorder getGuardInvocations() {
        guards.recorder
    }

    @Category(Class)
    static class ClassToTypeToken {
        TypeToken getType() {
            return TypeToken.of(this)
        }
    }

    @Category(TypeToken)
    static class TypeTokenToTypeToken {
        TypeToken getType() {
            this
        }
    }

}
