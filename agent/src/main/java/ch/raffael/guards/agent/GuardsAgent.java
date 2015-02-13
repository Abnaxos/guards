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

package ch.raffael.guards.agent;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ServiceLoader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import com.sun.tools.attach.VirtualMachine;

import ch.raffael.guards.NoNulls;
import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.asm.ClassReader;
import ch.raffael.guards.agent.asm.ClassWriter;
import ch.raffael.guards.agent.asm.util.TraceClassVisitor;
import ch.raffael.guards.agent.guava.base.Joiner;

import static ch.raffael.guards.agent.Logging.LOG;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsAgent {

    private final static GuardsAgent INSTANCE = new GuardsAgent();

    private final Transformer transformer = new Transformer();
    private final AtomicReference<Instrumentation> instrumentation = new AtomicReference<>(null);
    private volatile Options options = new Options();

    private final Object backgroundLock = new Object();
    private Thread backgroundThread = null;
    private final BlockingQueue<Runnable> backgroundQueue = new LinkedBlockingQueue<>();

    private GuardsAgent() {
    }

    public static GuardsAgent getInstance() {
        return INSTANCE;
    }

    private void install(Instrumentation instrumentation) {
        if ( !this.instrumentation.compareAndSet(null, instrumentation) ) {
            throw new IllegalStateException("Guards agent already initialized");
        }
        instrumentation.addTransformer(transformer, true);
        LOG.info("Guards Agent installed");
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        if ( options == null ) {
            throw new NullPointerException("options is null");
        }
        this.options = options;
    }

    public boolean isInstalled() {
        return instrumentation.get() != null;
    }

    public void configure(@Nullable @NoNulls OptionsProvider... providers) {
        OptionsBuilder builder = new OptionsBuilder();
        ServiceLoader<OptionsProvider> services = ServiceLoader.load(OptionsProvider.class);
        for( OptionsProvider provider : services ) {
            LOG.fine("Configuring from " + provider);
            provider.provideOptions(builder);
        }
        if ( providers != null && providers.length > 0 ) {
            for( OptionsProvider provider : providers ) {
                LOG.fine("Configuring from " + provider);
                provider.provideOptions(builder);
            }
        }
        builder.install();
    }

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        main(agentArgs, instrumentation);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        main(agentArgs, instrumentation);
        LOG.info("Retransforming all classes");
        for( Class<?> c : instrumentation.getAllLoadedClasses() ) {
            Transformer transformer = getInstance().transformer;
            if ( transformer.isTransformable(c) ) {
                try {
                    instrumentation.retransformClasses(c);
                }
                catch ( UnmodifiableClassException e ) {
                    LOG.log(Level.WARNING, "Cannot retransform " + c, e);
                }
            }
        }
        LOG.info("Retransformation done");
    }

    private static void main(String agentArgs, Instrumentation instrumentation) {
        INSTANCE.configure(new AgentArgsOptionsProvider(agentArgs));
        INSTANCE.install(instrumentation);
    }

    public static void installAgent(String agentArgs) {
        getInstance().doInstall(agentArgs);
    }

    private void doInstall(String agentArgs) {
        if ( !isInstalled() ) {
            try {
                Logging.LOG.info("Attempting to install agent dynamically");
                String jvmName = ManagementFactory.getRuntimeMXBean().getName();
                LOG.info("JVM Name: " + jvmName);
                int pos = jvmName.indexOf('@');
                if ( pos < 0 ) {
                    throw new RuntimeException("Cannot extract JVM ID from name " + jvmName);
                }
                String jvmId = jvmName.substring(0, pos);
                LOG.info("Trying JVM ID: " + jvmId);
                try {
                    VirtualMachine jvm = VirtualMachine.attach(jvmId);
                    LOG.info("Agent args: " + agentArgs);
                    jvm.loadAgent(locateAgentLib(), agentArgs);
                }
                catch ( NoClassDefFoundError e ) {
                    throw (ClassNotFoundException)new ClassNotFoundException(e.getMessage()).initCause(e);
                }
            }
            catch ( Exception e ) {
                LOG.log(Level.SEVERE, "Cannot install agent", e);
                throw new RuntimeException("Cannot install agent", e);
            }
        }
        else {
            LOG.info("Guards agent already installed, reconfiguring it");
            getInstance().configure(new AgentArgsOptionsProvider(agentArgs));
        }
    }

    private static String locateAgentLib() {
        CodeSource codeSource = GuardsAgent.class.getProtectionDomain().getCodeSource();
        if ( codeSource == null ) {
            throw new RuntimeException("Cannot locate agent library: Code source is null");
        }
        URL url = codeSource.getLocation();
        if ( url == null ) {
            throw new RuntimeException("Cannot locate agent library: Code source url is null");
        }
        String jarFileName = url.toString();
        if ( jarFileName.startsWith("jar:") && jarFileName.endsWith("!/") ) {
            jarFileName = jarFileName.substring(4, jarFileName.length() - 2);
        }
        File jarFile;
        if ( jarFileName.startsWith("file:") ) {
            jarFile = new File(URI.create(jarFileName));
        }
        else {
            throw new RuntimeException("Cannot deduce agent JAR from " + url);
        }
        if ( jarFile.isDirectory() && jarFile.toString().endsWith(File.separatorChar + Joiner.on(File.separatorChar).join("target", "classes", "main")) ) {
            jarFile = new File(jarFile.getParentFile().getParentFile(), "pseudo-agent.jar");
        }
        LOG.info("Trying agent JAR: " + jarFile);
        return jarFile.toString();
    }

    void submitToBackground(Runnable runnable) {
        synchronized ( backgroundLock ) {
            if ( backgroundThread == null || !backgroundThread.isAlive()) {
                Runnable loop = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Runnable runnable = null;
                            try {
                                runnable = backgroundQueue.take();
                            }
                            catch ( InterruptedException e ) {
                                // ignore
                            }
                            if ( runnable != null ) {
                                //Thread.currentThread().setDaemon(false);
                                try {
                                    runnable.run();
                                }
                                finally {
                                    //Thread.currentThread().setDaemon(true);
                                }
                            }
                        }
                        catch ( ThreadDeath e ) {
                            throw e;
                        }
                        catch ( Throwable e ) {
                            Thread thread = Thread.currentThread();
                            Thread.UncaughtExceptionHandler handler = thread.getUncaughtExceptionHandler();
                            if ( handler != null ) {
                                handler.uncaughtException(thread, e);
                            }
                            else {
                                e.printStackTrace(System.err);
                            }
                        }
                    }
                };
                backgroundThread = new Thread(loop, GuardsAgent.class.getName() + " Background Tasks");
                backgroundThread.setDaemon(true);
                backgroundThread.start();
            }
        }
        backgroundQueue.offer(runnable);
    }

    class Transformer implements ClassFileTransformer {

        private final String[] BUILTIN_EXCLUDES = {
                "java/", "sun/", "jdk/",
                "ch/raffael/guards/agent/"
                //, "ch/raffael/guards/definition/"
        };

        boolean isTransformable(ClassLoader loader, String internalName) {
            if ( loader == null ) {
                // NOTE: JDK8 supports transforming classes from the bootstrap class loader; in
                // JDK7, you need to add the agent's classes to the bootstrap classpath to make
                // this work (-Xbootclasspath/a:/path/to/agent.jar -javaagent:/path/to/agent.jar)
                //
                // However, I think it's perfectly OK to categorically exclude bootstrap classes.
                //
                // todo: Add an option for this
                return false;
            }
            for( String builtExclude : BUILTIN_EXCLUDES ) {
                if ( internalName.startsWith(builtExclude) ) {
                    return false;
                }
            }
            return true;
        }

        boolean isTransformable(Class<?> c) {
            if ( c.isPrimitive() || c.isArray()) {
                return false;
            }
            String internalName = c.getName().replace('.', '/');
            return isTransformable(c.getClassLoader(), internalName);
        }

        @Override
        @Nullable
        public byte[] transform(@Nullable ClassLoader loader, @NotNull final String className, @Nullable Class<?> classBeingRedefined, @Nullable ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if ( !isTransformable(loader, className) ) {
                return null;
            }
            try {
                final Options options = getInstance().getOptions();
                ClassReader classReader = new ClassReader(classfileBuffer);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                Instrumenter instrumenter = new Instrumenter(options, loader, classWriter);
                classReader.accept(instrumenter, ClassReader.SKIP_FRAMES);
                final byte[] instrumentedBytecode = classWriter.toByteArray();
                asmDump(options, className, instrumentedBytecode);
                return instrumentedBytecode;
            }
            catch ( CancelException e ) {
                //System.out.println("Cancel: " + className);
                return null;
            }
            catch ( Throwable e ) {
                e.printStackTrace();
                throw e;
            }
        }

        private void asmDump(@NotNull Options options, @NotNull String className, @NotNull byte[] instrumentedBytecode) {
            try {
                if ( !options.isDump() || options.getDumpPath() == null || options.getDumpFormats().isEmpty() ) {
                    return;
                }
                Path outPath = options.getDumpPath();
                String baseName = className.replace(".", options.getDumpPath().getFileSystem().getSeparator());
                outPath = outPath.resolve(baseName);
                baseName = outPath.getFileName().toString();
                outPath = outPath.getParent();
                for( Options.DumpFormat format : options.getDumpFormats() ) {
                    Files.createDirectories(outPath);
                    Path outFile = outPath.resolve(baseName + "." + format.extension());
                    switch ( format ) {
                        case CLASS:
                            Files.write(outFile, instrumentedBytecode);
                            break;
                        default:
                            try ( PrintWriter out = new PrintWriter(Files.newOutputStream(outFile)) ) {
                                new ClassReader(instrumentedBytecode).accept(new TraceClassVisitor(null, format.printer(), out), 0);
                            }
                    }
                }
            }
            catch ( IOException e ) {
                throw new RuntimeException(e.toString(), e);
            }
        }

    }
}
