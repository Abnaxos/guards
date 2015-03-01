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
import java.util.EnumSet;
import java.util.Set;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.guava.collect.Iterables;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("UnusedDeclaration")
public class OptionsBuilder {

    private boolean dump = false;
    private Path dumpPath = null;
    private final Set<Options.DumpFormat> dumpFormats = EnumSet.noneOf(Options.DumpFormat.class);

    private boolean xDevel = false;

    private boolean xUpgradeBytecode;
    private boolean xNopMode;
    private boolean xInstrumentAll;
    private Options.NopMethod xNopMethod;

    private boolean xMutableCallSites;

    public OptionsBuilder() {
        this(null);
    }

    public OptionsBuilder(@Nullable Options options) {
        if ( options == null ) {
            options = Options.DEFAULTS;
        }
        setXDevel(options.isXDevel());
        setDump(options.isDump());
        setDumpPath(options.getDumpPath());
        dumpFormats.addAll(options.getDumpFormats());
        setXUpgradeBytecode(options.isXUpgradeBytecode());
        setXNopMode(options.isXNopMode());
        setXInstrumentAll(options.isXInstrumentAll());
        setXNopMethod(options.getXNopMethod());
        setXMutableCallSites(options.isXMutableCallSites());
    }


    public boolean isDump() {
        return dump;
    }

    public void setDump(boolean dump) {
        this.dump = dump;
    }

    @NotNull
    public OptionsBuilder withDump(boolean dump) {
        setDump(dump);
        return this;
    }

    @Nullable
    public Path getDumpPath() {
        return dumpPath;
    }

    public void setDumpPath(@Nullable Path dumpPath) {
        this.dumpPath = dumpPath;
    }

    @NotNull
    public OptionsBuilder withDumpPath(@Nullable Path dumpPath) {
        setDumpPath(dumpPath);
        return this;
    }

    @NotNull
    public Set<Options.DumpFormat> getDumpFormats() {
        return dumpFormats;
    }

    @NotNull
    public OptionsBuilder withDumpFormat(@NotNull Options.DumpFormat dumpFormat) {
        getDumpFormats().add(dumpFormat);
        return this;
    }

    @NotNull
    public OptionsBuilder withDumpFormats(@NotNull Iterable<Options.DumpFormat> dumpFormats) {
        this.dumpFormats.clear();
        Iterables.addAll(this.dumpFormats, dumpFormats);
        return this;
    }

    public boolean isXDevel() {
        return xDevel;
    }

    public void setXDevel(boolean xDevel) {
        this.xDevel = xDevel;
    }

    @NotNull
    public OptionsBuilder withXDevel(boolean devel) {
        setXDevel(devel);
        return this;
    }

    public boolean isXUpgradeBytecode() {
        return xUpgradeBytecode;
    }

    public void setXUpgradeBytecode(boolean xUpgradeBytecode) {
        this.xUpgradeBytecode = xUpgradeBytecode;
    }

    public OptionsBuilder withXUpgradeBytecode(boolean xUpgradeBytecode) {
        setXUpgradeBytecode(xUpgradeBytecode);
        return this;
    }

    public boolean isXNopMode() {
        return xNopMode;
    }

    public void setXNopMode(boolean xNopMode) {
        this.xNopMode = xNopMode;
    }

    @NotNull
    public OptionsBuilder withXNopMode(boolean nopMode) {
        setXNopMode(nopMode);
        return this;
    }

    public boolean isXInstrumentAll() {
        return xInstrumentAll;
    }

    public void setXInstrumentAll(boolean xInstrumentAll) {
        this.xInstrumentAll = xInstrumentAll;
    }

    public OptionsBuilder withXInstrumentAll(boolean instrumentAll) {
        setXInstrumentAll(instrumentAll);
        return this;
    }

    public Options.NopMethod getXNopMethod() {
        return xNopMethod;
    }

    public void setXNopMethod(@NotNull Options.NopMethod xNopMethod) {
        this.xNopMethod = xNopMethod;
    }

    @NotNull
    public OptionsBuilder withXNopMethod(@NotNull Options.NopMethod nopMethod) {
        setXNopMethod(nopMethod);
        return this;
    }

    public boolean isXMutableCallSites() {
        return xMutableCallSites;
    }

    public void setXMutableCallSites(boolean xMutableCallSites) {
        this.xMutableCallSites = xMutableCallSites;
    }

    public OptionsBuilder withXMutableCallSites(boolean xMutableCallSites) {
        setXMutableCallSites(xMutableCallSites);
        return this;
    }

    @NotNull
    public Options toOptions() {
        return new Options(this);
    }

    @NotNull
    public Options install() {
        Options options = toOptions();
        GuardsAgent.getInstance().setOptions(options);
        return options;
    }

}
