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

package ch.raffael.guards.plugins.idea.ui.run;

import com.google.common.base.Objects;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.definition.PerformanceImpact;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsAgentSettings {

    private boolean enableGuardsAgent = false;
    private PerformanceImpact performanceImpact = PerformanceImpact.LOW;
    private MemberVisibility visibility = MemberVisibility.PRIVATE;

    private boolean useCustomAgent = false;
    private String customAgentPath = null;

    private boolean dumpClassFiles = false;
    private boolean dumpAsm = false;
    private boolean dumpBytecode = false;
    private String dumpPath = "guardAsmDumps";

    private boolean nopMode = false;
    private boolean instrumentAll = false;
    private boolean nopDedicatedMethod = false;
    private boolean mutableCallSites = false;

    public GuardsAgentSettings() {
    }

    public GuardsAgentSettings(@Nullable GuardsAgentSettings that) {
        if ( that != null ) {
            setEnableGuardsAgent(that.isEnableGuardsAgent());
            setPerformanceImpact(that.getPerformanceImpact());
            setVisibility(that.getVisibility());
            setUseCustomAgent(that.isUseCustomAgent());
            setCustomAgentPath(that.getCustomAgentPath());
            setDumpClassFiles(that.isDumpClassFiles());
            setDumpAsm(that.isDumpAsm());
            setDumpBytecode(that.isDumpBytecode());
            setDumpPath(that.getDumpPath());
            setNopMode(that.isNopMode());
            setInstrumentAll(that.isInstrumentAll());
            setNopDedicatedMethod(that.isNopDedicatedMethod());
            setNopMode(that.isNopMode());
            setMutableCallSites(that.isMutableCallSites());
        }
    }

    public boolean isEnableGuardsAgent() {
        return enableGuardsAgent;
    }

    public void setEnableGuardsAgent(boolean enableGuardsAgent) {
        this.enableGuardsAgent = enableGuardsAgent;
    }

    @NotNull
    public PerformanceImpact getPerformanceImpact() {
        return performanceImpact;
    }

    public void setPerformanceImpact(@NotNull PerformanceImpact performanceImpact) {
        this.performanceImpact = performanceImpact;
    }

    @NotNull
    public MemberVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(@NotNull MemberVisibility visibility) {
        this.visibility = visibility;
    }

    public boolean isUseCustomAgent() {
        return useCustomAgent;
    }

    public void setUseCustomAgent(boolean useCustomAgent) {
        this.useCustomAgent = useCustomAgent;
    }

    @Nullable
    public String getCustomAgentPath() {
        return customAgentPath;
    }

    public void setCustomAgentPath(@Nullable String customAgentPath) {
        this.customAgentPath = customAgentPath;
    }

    public boolean isDumpClassFiles() {
        return dumpClassFiles;
    }

    public void setDumpClassFiles(boolean dumpClassFiles) {
        this.dumpClassFiles = dumpClassFiles;
    }

    public boolean isDumpAsm() {
        return dumpAsm;
    }

    public void setDumpAsm(boolean dumpAsm) {
        this.dumpAsm = dumpAsm;
    }

    public boolean isDumpBytecode() {
        return dumpBytecode;
    }

    public void setDumpBytecode(boolean dumpBytecode) {
        this.dumpBytecode = dumpBytecode;
    }

    @NotNull
    public String getDumpPath() {
        return dumpPath;
    }

    public void setDumpPath(@NotNull String dumpPath) {
        this.dumpPath = dumpPath;
    }

    public boolean isNopMode() {
        return nopMode;
    }

    public void setNopMode(boolean nopMode) {
        this.nopMode = nopMode;
    }

    public boolean isInstrumentAll() {
        return instrumentAll;
    }

    public void setInstrumentAll(boolean instrumentAll) {
        this.instrumentAll = instrumentAll;
    }

    public boolean isNopDedicatedMethod() {
        return nopDedicatedMethod;
    }

    public void setNopDedicatedMethod(boolean nopDedicatedMethod) {
        this.nopDedicatedMethod = nopDedicatedMethod;
    }

    public boolean isMutableCallSites() {
        return mutableCallSites;
    }

    public void setMutableCallSites(boolean mutableCallSites) {
        this.mutableCallSites = mutableCallSites;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enableGuardsAgent,
                performanceImpact,
                visibility,
                useCustomAgent,
                customAgentPath,
                dumpClassFiles,
                dumpAsm,
                dumpBytecode,
                dumpPath,
                nopMode,
                instrumentAll,
                nopDedicatedMethod,
                mutableCallSites);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null || getClass() != obj.getClass() ) {
            return false;
        }
        final GuardsAgentSettings that = (GuardsAgentSettings)obj;
        return Objects.equal(this.enableGuardsAgent, that.enableGuardsAgent)
                && Objects.equal(this.performanceImpact, that.performanceImpact)
                && Objects.equal(this.visibility, that.visibility)
                && Objects.equal(this.useCustomAgent, that.useCustomAgent)
                && Objects.equal(this.customAgentPath, that.customAgentPath)
                && Objects.equal(this.dumpClassFiles, that.dumpClassFiles)
                && Objects.equal(this.dumpAsm, that.dumpAsm)
                && Objects.equal(this.dumpBytecode, that.dumpBytecode)
                && Objects.equal(this.dumpPath, that.dumpPath)
                && Objects.equal(this.nopMode, that.nopMode)
                && Objects.equal(this.instrumentAll, that.instrumentAll)
                && Objects.equal(this.nopDedicatedMethod, that.nopDedicatedMethod)
                && Objects.equal(this.mutableCallSites, that.mutableCallSites);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("enableGuardsAgent", enableGuardsAgent)
                .add("performanceImpact", performanceImpact)
                .add("visibility", visibility)
                .add("useCustomAgent", useCustomAgent)
                .add("customAgentPath", customAgentPath)
                .add("dumpClassFiles", dumpClassFiles)
                .add("dumpAsm", dumpAsm)
                .add("dumpBytecode", dumpBytecode)
                .add("dumpPath", dumpPath)
                .add("nopMode", nopMode)
                .add("instrumentAll", instrumentAll)
                .add("nopDedicatedMethod", nopDedicatedMethod)
                .add("mutableCallSites", mutableCallSites)
                .toString();
    }
}
