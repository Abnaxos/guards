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

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsPluginProject implements ProjectComponent {

    private final static String GUARDS_VIEW_ID = GuardsView.class.getName();

    private final Project project;

    private PsiElementTracker tracker;
    private GuardsView guardsView;
    private ToolWindow toolWindow;

    public GuardsPluginProject(Project project) {
        this.project = project;
    }

    public void initComponent() {
        guardsView = new GuardsView();
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "GuardsPluginProject";
    }

    public void projectOpened() {
        initToolWindow();
    }

    public void projectClosed() {
    }

    private void initToolWindow() {
        tracker = new PsiElementTracker(project, guardsView);
        tracker.start();
        ToolWindowManager twm = ToolWindowManager.getInstance(project);
        toolWindow = twm.getToolWindow(GUARDS_VIEW_ID);
        if ( toolWindow == null ) {
            toolWindow = twm.registerToolWindow(GUARDS_VIEW_ID, guardsView.getComponent(), ToolWindowAnchor.RIGHT);
            toolWindow.setTitle("Guards");
            toolWindow.setStripeTitle("Guards");
            toolWindow.setIcon(Icons.GUARDS_GENERAL);
        }
    }

    public GuardsView getGuardsView() {
        return guardsView;
    }

}
