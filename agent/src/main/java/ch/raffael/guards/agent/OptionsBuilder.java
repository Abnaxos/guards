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
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class OptionsBuilder {

    private boolean devel = false;

    private boolean dump = false;
    private Path dumpPath = null;
    private final Set<Options.DumpFormat> dumpFormats = EnumSet.noneOf(Options.DumpFormat.class);

    private boolean nopMode;
    private Options.NopMethod nopMethod;

    private Options.MultiGuardMethod multiGuardMethod;

    private boolean mutableCallSites;

    public OptionsBuilder() {
        this(null);
    }

    public OptionsBuilder(@Nullable Options options) {
        if ( options == null ) {
            options = Options.DEFAULTS;
        }
        setDevel(options.isDevel());
        setDump(options.isDump());
        setDumpPath(options.getDumpPath());
        dumpFormats.addAll(options.getDumpFormats());
        setNopMode(options.isNopMode());
        setNopMethod(options.getNopMethod());
        setMultiGuardMethod(options.getMultiGuardMethod());
        setMutableCallSites(options.isMutableCallSites());
    }


    public boolean isDevel() {
        return devel;
    }

    public void setDevel(boolean devel) {
        this.devel = devel;
    }

    @NotNull
    public OptionsBuilder withDevel(boolean devel) {
        setDevel(devel);
        return this;
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
    public OptionsBuilder withDumpFormats(@NotNull Collection<Options.DumpFormat> dumpFormats) {
        dumpFormats.clear();
        dumpFormats.addAll(dumpFormats);
        return this;
    }

    public boolean isNopMode() {
        return nopMode;
    }

    public void setNopMode(boolean nopMode) {
        this.nopMode = nopMode;
    }

    @NotNull
    public OptionsBuilder withNopMode(boolean nopMode) {
        setNopMode(nopMode);
        return this;
    }

    public Options.NopMethod getNopMethod() {
        return nopMethod;
    }

    public void setNopMethod(@NotNull Options.NopMethod nopMethod) {
        this.nopMethod = nopMethod;
    }

    @NotNull
    public OptionsBuilder withNopMethod(@NotNull Options.NopMethod nopMethod) {
        setNopMethod(nopMethod);
        return this;
    }

    public Options.MultiGuardMethod getMultiGuardMethod() {
        return multiGuardMethod;
    }

    public void setMultiGuardMethod(Options.MultiGuardMethod multiGuardMethod) {
        this.multiGuardMethod = multiGuardMethod;
    }

    public OptionsBuilder withMultiGuardMethod(Options.MultiGuardMethod multiGuardMethod) {
        setMultiGuardMethod(multiGuardMethod);
        return this;
    }

    public boolean isMutableCallSites() {
        return mutableCallSites;
    }

    public void setMutableCallSites(boolean mutableCallSites) {
        this.mutableCallSites = mutableCallSites;
    }

    public OptionsBuilder withMutableCallSites(boolean mutableCallSites) {
        setMutableCallSites(mutableCallSites);
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
