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

package ch.raffael.guards.plugins.idea.model;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;

import ch.raffael.guards.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardModelManager implements ModuleComponent {

    private final Module module;

    public GuardModelManager(Module module) {
        this.module = module;
    }

    @NotNull
    public static GuardModelManager get(Module module) {
        return checkNotNull(module.getComponent(GuardModelManager.class),
                "No GuardModelManager found for module " + module);
    }

    @NotNull
    public PsiGuardContext getContext() {
        return new PsiGuardContext(module);
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return GuardModelManager.class.getName();
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public void moduleAdded() {
    }

}
