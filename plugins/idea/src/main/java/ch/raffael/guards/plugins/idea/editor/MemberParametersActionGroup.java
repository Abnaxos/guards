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

package ch.raffael.guards.plugins.idea.editor;

import java.util.List;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiParameter;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ComponentNotRegistered")
public class MemberParametersActionGroup extends AbstractGuardPopupGroup<PsiMember> {

    private final List<PsiParameter> parameters;

    public MemberParametersActionGroup(GuardPopupController controller, PsiMember member, List<PsiParameter> parameters) {
        super(controller, member);
        this.parameters = parameters;
        add(asPopup(new ElementActionGroup(controller, member), true));
        if ( !parameters.isEmpty() ) {
            add(new Separator("Parameters"));
            for( PsiParameter parameter : parameters ) {
                add(asPopup(new ElementActionGroup(controller, parameter), true));
            }
        }
        getTemplatePresentation().setText(member.getName());
        getTemplatePresentation().setIcon(member.getIcon(0));
    }

    private <T extends ActionGroup> T asPopup(T action, boolean asPopup) {
        action.setPopup(asPopup);
        return action;
    }

}
