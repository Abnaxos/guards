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

import com.intellij.execution.CommonJavaRunConfigurationParameters;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.plugins.idea.GuardsApplicationComponent;

import static ch.raffael.guards.plugins.idea.ui.run.GuardsAgentSettingsEditor.SETTINGS_KEY;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsRunConfigurationExtension extends RunConfigurationExtension {

    public GuardsRunConfigurationExtension() {
    }

    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(T configuration, JavaParameters params, RunnerSettings runnerSettings) throws ExecutionException {
        if ( !isApplicableFor(configuration) ) {
            return;
        }
        GuardsAgentSettings settings = configuration.getUserData(SETTINGS_KEY);
        if ( settings == null || !settings.isEnableGuardsAgent() ) {
            return;
        }
        StringBuilder agentArg = new StringBuilder("-javaagent:");
        if ( settings.isUseCustomAgent() ) {
            agentArg.append(settings.getCustomAgentPath());
        }
        else {
            agentArg.append(GuardsApplicationComponent.getGuardsAgentJar().toString());
        }
        agentArg.append('=');
        if ( settings.isDumpClassFiles() || settings.isDumpAsm() || settings.isDumpBytecode() ) {
            agentArg.append("+dump,").append("dumpPath=").append(settings.getDumpPath()).append(',').append("dumpFormats=");
            boolean hasFormat = false;
            if ( settings.isDumpClassFiles() ) {
                agentArg.append("CLASS");
                hasFormat = true;
            }
            if ( settings.isDumpAsm() ) {
                if ( hasFormat ) {
                    agentArg.append('+');
                }
                agentArg.append("ASM");
            }
            if ( settings.isDumpBytecode() ) {
                if ( hasFormat ) {
                    agentArg.append('+');
                }
                agentArg.append("BYTECODE");
            }
            agentArg.append(',');
        }
        if ( settings.isNopMode() ) {
            agentArg.append("+XnopMode,");
        }
        if ( settings.isNopDedicatedMethod() ) {
            agentArg.append("XnopMethod=DEDICATED_METHOD,");
        }
        if ( settings.isInstrumentAll() ) {
            agentArg.append("+XinstrumentAll,");
        }
        if ( settings.isMutableCallSites() ) {
            agentArg.append("+XmutableCallSites,");
        }
        char lastChar = agentArg.charAt(agentArg.length() - 1);
        if ( lastChar == '=' || lastChar == ',' ) {
            agentArg.setLength(agentArg.length() - 1);
        }
        params.getVMParametersList().add(agentArg.toString());
    }

    @Override
    protected void readExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws InvalidDataException {
        GuardsAgentSettings settings = new GuardsAgentSettings();
        XmlSerializer.deserializeInto(settings, element);
        runConfiguration.putUserData(SETTINGS_KEY, settings);
    }

    @Override
    protected void writeExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws WriteExternalException {
        GuardsAgentSettings settings = runConfiguration.getUserData(SETTINGS_KEY);
        if ( settings != null ) {
            XmlSerializer.serializeInto(settings, element);
        }
    }


    @Nullable
    @Override
    protected <P extends RunConfigurationBase> SettingsEditor<P> createEditor(@NotNull P configuration) {
        return new GuardsAgentSettingsEditor<>(configuration.getProject());
    }

    @Nullable
    @Override
    protected String getEditorTitle() {
        return "Guards";
    }

    @Override
    protected boolean isApplicableFor(@NotNull RunConfigurationBase configuration) {
        return configuration instanceof CommonJavaRunConfigurationParameters;
    }

    @Override
    public void cleanUserData(RunConfigurationBase runConfigurationBase) {
        runConfigurationBase.putUserData(SETTINGS_KEY, null);
    }



}
