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

package ch.raffael.guards.agent;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import ch.raffael.guards.GuardsInternalError;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class Realm {

    private static final Map<ClassLoader, Realm> REALMS =
            new MapMaker().weakKeys().makeMap();
    private static final Realm SYSTEM_REALM = new Realm(ClassLoader.getSystemClassLoader());

    private final WeakReference<ClassLoader> loader;
    private final Map<String, String> mappings;
    private final Map<String, PackageInfo> packageInfos = new HashMap<>();
    private final Map<String, GuardHandle> guardHandles =
            new MapMaker().weakValues().makeMap();

    @SuppressWarnings("SuspiciousMethodCalls")
    private Realm(ClassLoader loader) {
        this.loader = new WeakReference<>(loader);
        Map<String, String> mappings = new HashMap<>();
        try {
            Enumeration<URL> mappingFiles = loader.getResources("META-INF/ch.raffael.guards.mappings.properties");
            while ( mappingFiles.hasMoreElements() ) {
                URL url = mappingFiles.nextElement();
                if ( Log.traceEnabled() ) {
                    Log.trace("Loading mappings from %s", url);
                }
                Properties properties = new Properties();
                try ( InputStream in = new BufferedInputStream(url.openStream()) ) {
                    properties.load(in);
                }
                for ( Map.Entry<Object, Object> entry : properties.entrySet() ) {
                    if ( !mappings.containsKey(entry.getKey()) ) {
                        mappings.put((String)entry.getKey(), (String)entry.getValue());
                    }
                }
            }
            mappings = ImmutableMap.copyOf(mappings);
        }
        catch ( IOException e ) {
            Log.error("Error loading mappings for %s", e, loader);
            mappings = Collections.emptyMap();
        }
        this.mappings = mappings;
    }

    static Realm get(ClassLoader loader) {
        if ( GuardsTransformer.isSystemClassLoader(loader) ) {
            return SYSTEM_REALM;
        }
        synchronized ( REALMS ) {
            Realm realm = REALMS.get(loader);
            if ( realm == null ) {
                realm = new Realm(loader);
                REALMS.put(loader, realm);
            }
            return realm;
        }
    }

    PackageInfo getPackageInfo(String packageName) {
        synchronized ( packageInfos ) {
            PackageInfo info = packageInfos.get(packageName);
            if ( info == null ) {
                if ( packageName.equals("") ) {
                    info = new PackageInfo(null, "", Collections.<Class<? extends Annotation>, ClassScanner.GuardDeclaration>emptyMap());
                    packageInfos.put("", info);
                }
                else {
                    PackageInfo parentInfo;
                    int pos = packageName.lastIndexOf('/');
                    if ( pos >= 0 ) {
                        parentInfo = getPackageInfo(packageName.substring(0, pos));
                    }
                    else {
                        parentInfo = getPackageInfo("");
                    }
                    info = new PackageInfo(parentInfo, packageName, loadPackageGuards(parentInfo, packageName));
                }
            }
            return info;
        }
    }

    @SuppressWarnings("ObjectEquality")
    GuardHandle getGuardHandle(String binaryName) {
        ClassLoader loader = loader();
        GuardHandle handle;
        String mapped = mappings.get(binaryName);
        if ( mapped != null ) {
            binaryName = mapped;
        }
        synchronized ( guardHandles ) {
            handle = guardHandles.get(binaryName);
            if ( handle == null ) {
                Class<?> annotationClass;
                try {
                    annotationClass = Class.forName(binaryName, false, loader);
                }
                catch ( ClassNotFoundException e ) {
                    Log.trace("Cannot load annotation class %s", binaryName);
                    return null;
                }
                handle = GuardHandle.load(annotationClass);
                guardHandles.put(binaryName, handle);
            }
        }
        if ( handle == GuardHandle.NOT_A_GUARD ) {
            return null;
        }
        else {
            return handle;
        }
    }

    private Map<Class<? extends Annotation>, ClassScanner.GuardDeclaration> loadPackageGuards(PackageInfo parentPackage, final String packageName) {
        final ClassLoader loader = loader();
        try ( InputStream packageInfoStream = loader.getResourceAsStream(packageName + "/package-info.class") ) {
            if ( packageInfoStream == null ) {
                return parentPackage.packageGuards;
            }
            final Map<Class<? extends Annotation>,ClassScanner.GuardDeclaration> packageGuards = new HashMap<>(parentPackage.packageGuards);
            new ClassReader(packageInfoStream).accept(new ClassVisitor(Opcodes.ASM4) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if ( desc.equals(Types.T_REPEAL.getDescriptor()) ) {
                        return new AnnotationVisitor(Opcodes.ASM4) {
                            @Override
                            public AnnotationVisitor visitArray(String name) {
                                if ( name.equals("value") ) {
                                    return this;
                                }
                                else {
                                    Log.error("Invalid @Repeal annotation encountered in package %s", packageName);
                                    return null;
                                }
                            }
                            @SuppressWarnings("SuspiciousMethodCalls")
                            @Override
                            public void visit(String name, Object value) {
                                if ( !(value instanceof Type) ) {
                                    Log.error("Invalid @Repeal annotation encountered in package %s", packageName);
                                }
                                else {
                                    Type type = (Type)value;
                                    if ( type.getSort() == Type.OBJECT ) {
                                        try {
                                            Class<?> clazz = Class.forName(type.getClassName(), false, loader);
                                            Log.debug("Repealing from package %s: %s", packageName, clazz);
                                            packageGuards.remove(clazz);
                                        }
                                        catch ( ClassNotFoundException e ) {
                                            if ( Log.traceEnabled() ) {
                                                Log.trace("Cannot load class for @Repeal in package %s: %s", packageName, type);
                                            }
                                        }
                                    }
                                    else {
                                        if ( Log.traceEnabled() ) {
                                            Log.trace("Skipping type for @Repeal in package %s: %s", packageName, type);
                                        }
                                    }
                                }
                            }
                        };
                    }
                    else {
                        GuardHandle guardHandle = getGuardHandle(Type.getType(desc).getClassName());
                        if ( guardHandle != null ) {
                            ClassScanner.GuardDeclaration declaration = new ClassScanner.GuardDeclaration(Realm.this, guardHandle);
                            packageGuards.put(guardHandle.getAnnotationClass(), declaration);
                            return declaration;
                        }
                        else {
                            return null;
                        }
                    }
                }
            }, 0);
            return packageGuards;
        }
        catch ( IOException e ) {
            Log.error("Error loading package-info.java for package %s", e, packageName);
            return Collections.emptyMap();
        }
    }

    ClassLoader loader() {
        ClassLoader loader = this.loader.get();
        if ( loader == null ) {
            // FIXME: is it true that this cannot happen?
            throw new GuardsInternalError("Lost class loader");
        }
        return loader;
    }

    static class PackageInfo {
        final PackageInfo parent;
        final String name;
        final Map<Class<? extends Annotation>,ClassScanner.GuardDeclaration> packageGuards;
        PackageInfo(PackageInfo parent, String name, Map<Class<? extends Annotation>, ClassScanner.GuardDeclaration> packageGuards) {
            this.parent = parent;
            this.name = name;
            this.packageGuards = packageGuards;
        }
    }

}
