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
import org.jetbrains.annotations.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsPlugin implements ApplicationComponent, InspectionToolProvider {

    public static final boolean DEBUG = true;

    public static final String PLUGIN_ID = "ch.raffael.guards";

    public GuardsPlugin() {
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return PLUGIN_ID + "." + GuardsPlugin.class.getSimpleName();
    }

    @Override
    public Class[] getInspectionClasses() {
        return new Class[] {
                GuardsInheritanceInspection.class
        };
    }
}