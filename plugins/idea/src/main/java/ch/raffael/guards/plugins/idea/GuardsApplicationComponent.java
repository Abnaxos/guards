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

package ch.raffael.guards.plugins.idea;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static ch.raffael.guards.plugins.idea.GuardsApplicationComponent.PLUGIN_ID;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@State(name = PLUGIN_ID + ".UiState",
        storages = @Storage(file = StoragePathMacros.APP_CONFIG + "/guards.xml"))
public class GuardsApplicationComponent implements ApplicationComponent, InspectionToolProvider, PersistentStateComponent<UiState> {

    public static final boolean DEBUG = true;

    public static final String PLUGIN_ID = "ch.raffael.guards";

    private UiState uiState = new UiState();

    public GuardsApplicationComponent() {
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return PLUGIN_ID + "." + GuardsApplicationComponent.class.getSimpleName();
    }

    @Override
    public Class[] getInspectionClasses() {
        return new Class[] {
                GuardsInheritanceInspection.class
        };
    }

    public UiState getUiState() {
        return uiState;
    }

    @Nullable
    @Override
    public UiState getState() {
        return uiState;
    }

    @Override
    public void loadState(UiState state) {
        this.uiState = state;
    }

}
