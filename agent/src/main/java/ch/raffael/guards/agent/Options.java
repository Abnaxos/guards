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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.asm.util.ASMifier;
import ch.raffael.guards.agent.asm.util.Printer;
import ch.raffael.guards.agent.asm.util.Textifier;
import ch.raffael.guards.agent.guava.collect.Sets;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
public final class Options {

    public static final Options DEFAULTS = new Options();

    private boolean dump = false;
    private Path dumpPath = Paths.get("guardsAsmDumps");
    private Set<DumpFormat> dumpFormats = Sets.immutableEnumSet(Options.DumpFormat.CLASS, Options.DumpFormat.ASM);

    private boolean xDevel = false;

    private boolean xUpgradeBytecode = true;
    private boolean xNopMode = false;
    private boolean xInstrumentAll = false;
    private NopMethod xNopMethod = NopMethod.MH_CONSTANT;

    private boolean xMutableCallSites = false;

    public Options() {
        this(null);
    }

    public Options(@Nullable OptionsBuilder builder) {
        if ( builder != null ) {
            dump = builder.isDump();
            dumpPath = builder.getDumpPath();
            dumpFormats = Sets.immutableEnumSet(builder.getDumpFormats());
            xDevel = builder.isXDevel();
            xUpgradeBytecode = builder.isXUpgradeBytecode();
            xNopMode = builder.isXNopMode();
            xInstrumentAll = builder.isXInstrumentAll();
            xNopMethod = builder.getXNopMethod();
            xMutableCallSites = builder.isXMutableCallSites();
        }
    }

    public boolean isDump() {
        return dump;
    }

    @Nullable
    public Path getDumpPath() {
        return dumpPath;
    }

    @NotNull
    public Set<DumpFormat> getDumpFormats() {
        return dumpFormats;
    }

    public boolean isXDevel() {
        return xDevel;
    }

    public boolean isXUpgradeBytecode() {
        return xUpgradeBytecode;
    }

    public boolean isXNopMode() {
        return xNopMode;
    }

    public boolean isXInstrumentAll() {
        return xInstrumentAll;
    }

    @NotNull
    public NopMethod getXNopMethod() {
        return xNopMethod;
    }

    /**
     * Use `MutableCallSite` instead of `ConstantCallSite` for guard invocations. `MutableCallSite`
     * would be an elegant solution to switch guards on and off at runtime by just switching the
     * call site to *nop* or back to a real call site.
     *
     * **[Performance]** First simplistic benchmarks seem to indicate that using `MutableCallSite`
     * is actually a viable option.
     */
    public boolean isXMutableCallSites() {
        return xMutableCallSites;
    }

    public static enum DumpFormat {
        CLASS("class") {
            @Override
            Printer printer() {
                return null;
            }
        },
        ASM("java") {
            @Override
            Printer printer() {
                return new ASMifier();
            }
        },
        BYTECODE("cafebabe") {
            @Override
            Printer printer() {
                return new Textifier();
            }
        };

        private final String extension;

        DumpFormat(String extension) {
            this.extension = extension;
        }

        public String extension() {
            return extension;
        }

        abstract Printer printer();

    }

    public static enum NopMethod {
        /**
         * Use `MethodHandles.constant(Void.class, null).asType(methodType(void.class))` as for
         * creating a do-nothing method handle.
         *
         * **Performance:** First simplistic benchmarks indicate that it makes no difference which
         * method is used.
         */
        MH_CONSTANT,
        /**
         * Use a dedicated `void nop()` method for creating a do-nothing method handle.
         *
         * **Performance:** First simplistic benchmarks indicate that it makes no difference which
         * method is used.
         */
        DEDICATED_METHOD
    }

}
