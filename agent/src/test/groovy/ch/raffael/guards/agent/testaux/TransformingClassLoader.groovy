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





package ch.raffael.guards.agent.testaux

import ch.raffael.guards.agent.GuardsTransformer
import groovy.transform.CompileStatic

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@CompileStatic
class TransformingClassLoader extends ClassLoader {

    static final String TF_PREFIX = "ch.raffael.guards.test.transform."

    public final GuardsTransformer transformer = new GuardsTransformer()

    TransformingClassLoader(ClassLoader classLoader) {
        super(classLoader)
        transformer.parseArgumentString(System.getProperty('agentArgs', ''))
        transformer.setMode(GuardsTransformer.Mode.EXCEPTION)
        assertions(true)
    }

    TransformingClassLoader() {
        this(TransformingClassLoader.class.getClassLoader())
    }

    void assertions(boolean assertionStatus) {
        setPackageAssertionStatus("ch.raffael.guards.test.transform", assertionStatus)
    }

    Class<?> load(String name) {
        name = TF_PREFIX + name
        loadClass(name, false)
    }

    def create(String name, Object... args) {
        load(name).newInstance(args)
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if ( name.startsWith(TF_PREFIX) ) {
            // take over
            Class cls = null
            synchronized ( getClassLoadingLock(name) ) {
                cls = findLoadedClass(name)
                if ( cls == null ) {
                    cls = findClass(name)
                }
            }
            if ( cls != null ) {
                if ( resolve ) {
                    resolveClass(cls)
                }
                return cls
            }
        }
        super.loadClass(name, resolve)
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        InputStream bytecodeStream = getResourceAsStream("${name.replace('.', '/')}.class")
        if ( bytecodeStream != null ) {
            try {
                byte[] bytecode = bytecodeStream.getBytes()
                byte[] newBytecode = transformer.transform(this, name.replace('.', '/'), null, null, bytecode)
                if ( newBytecode != null ) {
                    bytecode = newBytecode
                }
                return defineClass(name, bytecode, 0, bytecode.length)
            }
            catch ( IOException e ) {
                throw new ClassNotFoundException(name, e)
            }
        }
        else {
            return super.findClass(name)
        }
    }
}
