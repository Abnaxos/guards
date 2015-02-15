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

import ch.raffael.guards.agent.guava.reflect.TypeToken
import org.codehaus.groovy.control.CompilerConfiguration

import static groovy.lang.Closure.DELEGATE_FIRST


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class DynaGuards {

    static final String PACKAGE = 'ch.raffael.guards.test.$dynamic'
    static final String DEFAULT_GUARD = 'Default'

    final Recorder recorder
    private methods

    private final Map<String, GuardSpec> guardSpecs = [:].withDefault { String name -> new GuardSpec(name) }
    private final Map<String, MethodSpec> methodSpecs = [:].withDefault {[:]}.withDefault { String name -> new MethodSpec(name)}
    private int testCounter = 0

    DynaGuards(Recorder recorder) {
        this.recorder = recorder
    }

    def guard(@DelegatesTo(value = GuardSpec, strategy = DELEGATE_FIRST) Closure config = null) {
        guard(DEFAULT_GUARD, config)
    }

    def guard(String name, @DelegatesTo(value = GuardSpec, strategy = DELEGATE_FIRST) Closure config = null) {
        with(guardSpecs[name], config)
    }

    def GuardSpec getGuard() {
        guard(DEFAULT_GUARD)
    }

    def method(String name, Closure config = null) {
        with(methodSpecs[name], config)
    }

    def getMethods() {
        if ( methods == null ) {
            StringWriter string = new StringWriter()
            PrintWriter out = new PrintWriter(string)
            out.println "package $PACKAGE"
            out.println "import ch.raffael.guards.definition.*"
            out.println "import ch.raffael.guards.agent.guava.reflect.TypeToken"
            out.println "import ch.raffael.guards.agent.guava.reflect.TypeToken"
            guardSpecs.values().each { spec -> spec.toSource(out) }
            out.println()
            out.println "class METHODS {"
            out.println "static ch.raffael.guards.agent.DynaGuards.Recorder RECORDER"
            methodSpecs.values().each { spec -> spec.toSource(out) }
            out.println "}"
            out.println "new METHODS()"
            out.flush()
            def source = string.toString()
            println '| *** BEGIN DynaGuards Source Code'
            source.eachLine { line -> println '| ' + line }
            println()
            methods = new GroovyShell(DynaGuards.classLoader, new Binding(), new CompilerConfiguration(
                    targetBytecode: CompilerConfiguration.JDK7)).evaluate(source)
            methods.RECORDER = recorder
        }
        return methods
    }

    void invoke(Closure closure) {
        getMethods().with(closure)
    }

    static <T> T with(T delegate, Closure closure) {
        if ( closure != null ) {
            //noinspection GroovyAssignabilityCheck
            closure = closure.clone()
            closure.delegate = delegate
            closure.resolveStrategy = DELEGATE_FIRST
            closure.call()
        }
        return delegate
    }

    private static typeSource(TypeToken type) {
        if ( type.primitive ) {
            return "TypeToken.of(${type.rawType.name}.class)"
        }
        else {
            return "new TypeToken<$type>(){}"
        }
    }

    class GuardSpec {
        final String name
        final Map<String, TestSpec> tests = [:]

        GuardSpec(String name) {
            this.name = name
        }

        def test(Class type) {
            test(TypeToken.of(type))
        }

        def test(String name, Class type) {
            test(name, TypeToken.of(type))
        }

        def test(TypeToken type) {
            test("test${testCounter++}", type)
        }

        def test(String name, TypeToken type) {
            tests.get(name, new TestSpec(name, type))
        }

        private toSource(PrintWriter out) {
            out.println "@java.lang.annotation.Target(java.lang.annotation.ElementType.PARAMETER)"
            out.println "@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)"
            out.println "@Guard(performanceImpact=PerformanceImpact.LOW)"
            out.println "@interface $name {"
            out.println "class Handler extends Guard.Handler<$name> {"
            tests.values().each { t -> t.toSource(out) }
            out.println "}}"
        }

    }

    class TestSpec {
        final String name
        final TypeToken type

        TestSpec(String name, TypeToken type) {
            this.name = name
            this.type = type
        }
        private toSource(PrintWriter out) {
            out.println "@Guard.Handler.Test static boolean $name($type value) {"
            out.println "  METHODS.RECORDER.invocation('$name', ${typeSource(type)}, value)"
            out.println "  return true"
            out.println "}"
        }

    }

    class MethodSpec {
        final String name
        private final Map<String, ParamSpec> params = [:]
        private int paramCounter = 0

        MethodSpec(String name) {
            this.name = name
        }

        def param(TypeToken type, @DelegatesTo(value = ParamSpec, strategy = DELEGATE_FIRST) Closure config = null) {
            param(type, "arg${paramCounter++}", config)
        }

        def param(TypeToken type, String name, @DelegatesTo(value = ParamSpec, strategy = DELEGATE_FIRST) Closure config = null) {
            with(params.get(name, new ParamSpec(name, type)), config)
        }

        def param(Class type, @DelegatesTo(value = ParamSpec, strategy = DELEGATE_FIRST) Closure config = null) {
            param(TypeToken.of(type), "arg${paramCounter++}", config)
        }

        def param(Class type, String name, @DelegatesTo(value = ParamSpec, strategy = DELEGATE_FIRST) Closure config = null) {
            param(TypeToken.of(type), name, config)
        }

        private toSource(PrintWriter out) {
            out.print "def $name("
            boolean first = true
            params.values().each { param ->
                param.toSource(out)
                if ( first ) {
                    first = false
                }
                else {
                    out.print ', '
                }
            }
            out.println ") {}"
        }
    }

    class ParamSpec {
        final String name
        final TypeToken type
        final Set<GuardSpec> guards = []

        ParamSpec(String name, TypeToken type) {
            this.name = name
            this.type = type
        }

        def guard(String name = DEFAULT_GUARD) {
            if ( !DynaGuards.this.guardSpecs.containsKey(name) ) {
                throw new IllegalArgumentException("No such guard: $name")
            }
            guards << DynaGuards.this.guardSpecs[name]
        }

        protected toSource(PrintWriter out) {
            guards.each { guard -> out.print "@$guard.name "}
            out.print "$type $name"
        }
    }

    static interface Recorder {

        void invocation(String name, TypeToken type, Object value)

    }
}
