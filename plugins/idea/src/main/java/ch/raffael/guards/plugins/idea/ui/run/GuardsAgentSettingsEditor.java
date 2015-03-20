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

import javax.swing.JComponent;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsAgentSettingsEditor<P extends RunConfigurationBase> extends SettingsEditor<P> {

    static final Key<GuardsAgentSettings> SETTINGS_KEY = new Key<>(GuardsAgentSettings.class.getName());

    private final GuardsAgentSettingsForm editor;

    public GuardsAgentSettingsEditor(Project project) {
        editor = new GuardsAgentSettingsForm(project);
    }

    @Override
    protected void resetEditorFrom(RunConfigurationBase s) {
        editor.setSettings(s.getUserData(SETTINGS_KEY));
    }

    @Override
    protected void applyEditorTo(RunConfigurationBase s) throws ConfigurationException {
        s.putUserData(SETTINGS_KEY, editor.getSettings());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return editor.getComponent();
    }
}
