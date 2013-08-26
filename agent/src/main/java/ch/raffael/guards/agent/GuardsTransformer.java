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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class GuardsTransformer implements ClassFileTransformer {

    private static final Set<ClassLoader> SYSTEM_CLASS_LOADERS;
    static {
        ImmutableSet.Builder<ClassLoader> builder = ImmutableSet.builder();
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        while ( loader != null ) {
            builder.add(loader);
            loader = loader.getParent();
        }
        SYSTEM_CLASS_LOADERS = builder.build();
    }

    private volatile Path dumpPath = null;
    private volatile DumpFormat dumpFormat = DumpFormat.BYTECODE;
    private volatile Mode mode = Mode.ASSERT;

    public GuardsTransformer() {
    }

    public static void premain(String args, Instrumentation instrumentation) {
        GuardsTransformer agent = new GuardsTransformer();
        agent.parseArgumentString(args);
        instrumentation.addTransformer(agent);
        Log.info("Guards agent installed (mode %s)", agent.getMode());
        if ( agent.getDumpPath() != null ) {
            Log.info("Dumping to %s using format %s", agent.getDumpPath(), agent.getDumpFormat());
        }
    }

    public void parseArgumentString(String args) {
        if ( args == null ) {
            return;
        }
        for ( String arg : args.split(",") ) {
            String argName;
            String argValue = null;
            int pos = arg.indexOf('=');
            if ( pos < 0 ) {
                argName = arg.trim();
            }
            else {
                argName = arg.substring(0, pos).trim();
                argValue = arg.substring(pos + 1).trim();
            }
            switch ( argName ) {
                case "dumpPath":
                    if ( argValue == null ) {
                        Log.error("Parameter 'dumpPath' requires a path");
                    }
                    else {
                        setDumpPath(Paths.get(argValue));
                    }
                    break;
                case "dumpFormat":
                    if ( argValue == null ) {
                        Log.error("Parameter 'dumpFormat' requires a format ('bytecode' or 'asm')");
                    }
                    else {
                        try {
                            setDumpFormat(DumpFormat.valueOf(argValue.toUpperCase()));
                        }
                        catch ( IllegalArgumentException e ) {
                            Log.error("Invalid dump format '%s' ('bytecode' or 'asm' supported)", argValue);
                        }
                    }

                    break;
                case "mode":
                    if ( argValue == null ) {
                        Log.error("Parameter 'mode' requires a format ('assert' or 'exception')");
                    }
                    else {
                        try {
                            setMode(Mode.valueOf(argValue.toUpperCase()));
                        }
                        catch ( IllegalArgumentException e ) {
                            Log.error("Invalid dump format '%s' ('bytecode' or 'asm' supported)", argValue);
                        }
                    }

                    break;
                default:
                    Log.error("Unknown parameter '%s'", argName);
            }
        }
    }

    public Path getDumpPath() {
        return dumpPath;
    }

    public void setDumpPath(Path dumpPath) {
        this.dumpPath = dumpPath;
    }

    public DumpFormat getDumpFormat() {
        return dumpFormat;
    }

    public void setDumpFormat(DumpFormat dumpFormat) {
        this.dumpFormat = dumpFormat;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] bytecode) throws IllegalClassFormatException {
        if ( loader == null || className.startsWith("ch/raffael/guards/agent/") || !isInstrumentable(loader) ) {
            // skip instrumentation if:
            // - the class loader is the bootstrap class loader
            // - it's one of the agent's core classes
            // - CheckerStore isn't visible from that class loader
            if ( Log.traceEnabled() ) {
                Log.trace("NOT instrumenting class %s", className);
            }
            return null;
        }
        if ( Log.debugEnabled() ) {
            Log.debug("Instrumenting class: %s", className);
        }
        Realm realm = Realm.get(loader);
        Type type = Type.getObjectType(className);
        // find outermost class
        try {
            Type outermostType = findOutermost(loader, bytecode);
            if ( outermostType == null ) {
                outermostType = type;
            }
            CheckerStore checkerStore = CheckerStore.storeFor(loader, type.getClassName());
            ClassScanner scanner = new ClassScanner(realm);
            new ClassReader(bytecode).accept(scanner, 0);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
            FieldVisitor fv = classWriter.visitField(Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, Types.F_CHECKER_STORE, Types.T_CHECKER_STORE.getDescriptor(), null, null);
            fv.visitEnd();
            if ( mode == Mode.ASSERT ) {
                fv = classWriter.visitField(Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, Types.F_ASSERTIONS_ENABLED, Type.BOOLEAN_TYPE.getDescriptor(), null, null);
                fv.visitEnd();
            }
            ClassVisitor visitor = classWriter;
            visitor = new InitClassVisitor(visitor, type);
            visitor = new Instrumenter(visitor, outermostType, mode, checkerStore, scanner);
            //visitor = new GuardsClassVisitor(loader, Type.getType("L" + (outermostName != null ? outermostName : className) + ";"), visitor);
            new ClassReader(bytecode).accept(visitor, ClassReader.EXPAND_FRAMES);
            byte[] instrumented = classWriter.toByteArray();
            if ( dumpPath != null ) {
                DumpFormat format = dumpFormat;
                Path target = dumpPath.resolve(className.replace("/", dumpPath.getFileSystem().getSeparator()) + "." + format.extension());
                if ( Log.traceEnabled() ) {
                    Log.trace("Dumping to %s", target);
                }
                Files.createDirectories(target.getParent());
                try ( PrintWriter writer = new PrintWriter(Files.newBufferedWriter(target, Charset.defaultCharset())) ) {
                    new ClassReader(instrumented).accept(new TraceClassVisitor(null, format.printer(), writer), 0);
                }
            }
            return instrumented;
        }
        catch ( InstrumentationException |IOException e ) {
            Log.error("Error instrumenting class %s", e, type.getClassName());
            return null;
        }
        catch ( RuntimeException | Error e ) {
            Log.error("Error instrumenting class %s", e, type.getClassName());
            throw e;
        }
    }

    private boolean isInstrumentable(ClassLoader loader) {
        if ( loader instanceof ClassSynthesizer ) {
            return false;
        }
        try {
            loader.loadClass(CheckerStore.class.getName());
            return true;
        }
        catch ( ClassNotFoundException e ) {
            return false;
        }
    }

    private Type findOutermost(final ClassLoader loader, byte[] bytecode) throws InstrumentationException {
        class OuterScanner extends ClassVisitor {
            String outer;
            InstrumentationException exception;
            OuterScanner() {
                super(Opcodes.ASM4);
            }
            @Override
            public void visitOuterClass(String owner, String name, String desc) {
                outer = name;
                try ( InputStream input = loader.getResourceAsStream(name + ".class") ) {
                    if ( input != null ) {
                        new ClassReader(input).accept(this, 0);
                    }
                    else {
                        exception = new InstrumentationException("Outer class " + name + " not found");
                    }
                }
                catch ( IOException e ) {
                    exception = new InstrumentationException("Error reading outer class " + name, e);
                }
            }
        }
        OuterScanner outerScanner = new OuterScanner();
        new ClassReader(bytecode).accept(outerScanner, 0);
        if ( outerScanner.exception != null ) {
            throw outerScanner.exception;
        }
        if ( outerScanner.outer != null ) {
            return Type.getObjectType(outerScanner.outer);
        }
        else {
            return null;
        }
    }

    public static boolean isSystemClassLoader(ClassLoader loader) {
        return loader == null || SYSTEM_CLASS_LOADERS.contains(loader);
    }

    private class InitClassVisitor extends ClassVisitor {

        private final Type type;
        private boolean clinitVisited;

        public InitClassVisitor(ClassVisitor cv, Type type) {
            super(Opcodes.ASM4, cv);
            this.type = type;
            clinitVisited = false;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if ( "<clinit>".equals(name) && !clinitVisited ) {
                clinitVisited = true;
                return visitClInit(mv);
            }
            else {
                return mv;
            }
        }

        @Override
        public void visitEnd() {
            if ( !clinitVisited ) {
                MethodVisitor mv = super.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                mv = visitClInit(mv);
                mv.visitCode();
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
            super.visitEnd();
        }

        private MethodVisitor visitClInit(MethodVisitor mv) {
            return new GeneratorAdapter(Opcodes.ASM4, mv, Opcodes.ACC_STATIC, "<clinit>", "()V") {
                @Override
                public void visitCode() {
                    if ( mode == Mode.ASSERT ) {
                        push(type);
                        invokeVirtual(Types.T_CLASS, Types.M_ASSERTION_STATUS);
                        putStatic(type, Types.F_ASSERTIONS_ENABLED, Type.BOOLEAN_TYPE);
                    }
                    push(type);
                    invokeVirtual(Types.T_CLASS, Types.M_GET_CLASS_LOADER);
                    push(type.getClassName());
                    invokeStatic(Types.T_CHECKER_STORE, Types.M_CHECKER_STORE_RETRIEVE);
                    putStatic(type, Types.F_CHECKER_STORE, Types.T_CHECKER_STORE);
                    super.visitCode();
                }
                //@Override
                //public void visitMaxs(int maxStack, int maxLocals) {
                //    super.visitMaxs(Math.max(2, maxStack), Math.max(0, maxLocals));
                //}
            };
        }
    }

    public static enum Mode {
        ASSERT, EXCEPTION
    }

    public static enum DumpFormat {
        BYTECODE("cafebabe") {
            @Override
            public Textifier printer() {
                return new Textifier();
            }
        },
        ASM("java") {
            @Override
            public ASMifier printer() {
                return new ASMifier();
            }
        };

        private final String extension;
        private DumpFormat(String extension) {
            this.extension = extension;
        }
        public String extension() {
            return extension;
        }
        public abstract Printer printer();
    }

}
