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

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.asm.ClassReader;
import ch.raffael.guards.agent.asm.ClassWriter;
import ch.raffael.guards.agent.asm.util.TraceClassVisitor;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class AgentTransformer implements ClassFileTransformer {

    private static final String[] BUILTIN_EXCLUDES = {
            "java/", "sun/", "jdk/",
            "ch/raffael/guards/agent/"
            //, "ch/raffael/guards/definition/"
    };

    @Override
    @Nullable
    public byte[] transform(@Nullable ClassLoader loader, @NotNull final String className, @Nullable Class<?> classBeingRedefined, @Nullable ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        for( String builtExclude : BUILTIN_EXCLUDES ) {
            if ( className.startsWith(builtExclude) ) {
                return null;
            }
        }
        try {
            final Options options = GuardsAgent.getInstance().getOptions();
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
