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

package ch.raffael.guards.plugins.idea.ui.live;

import java.util.List;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.psi.PsiNamedElement;

import ch.raffael.guards.plugins.idea.psi.PsiGuardTarget;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ComponentNotRegistered")
public class ParametersActionGroup extends AbstractGuardPopupGroup<PsiGuardTarget> {

    private final List<PsiGuardTarget> parameters;

    public ParametersActionGroup(GuardPopupController controller, PsiGuardTarget parent) {
        super(controller, parent);
        this.parameters = parent.getParameters().toList();
        add(asPopup(new ElementActionGroup(controller, parent), true));
        if ( !parameters.isEmpty() ) {
            add(new Separator("Parameters"));
            for( PsiGuardTarget parameter : parameters ) {
                add(asPopup(new ElementActionGroup(controller, parameter), true));
            }
        }
        getTemplatePresentation().setText(((PsiNamedElement)parent.getElement()).getName());
        getTemplatePresentation().setIcon(parent.getElement().getIcon(0));
    }

    private <T extends ActionGroup> T asPopup(T action, boolean asPopup) {
        action.setPopup(asPopup);
        return action;
    }

}
