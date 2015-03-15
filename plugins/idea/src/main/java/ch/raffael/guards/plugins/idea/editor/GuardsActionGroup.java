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

import com.google.common.base.Function;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierListOwner;

import ch.raffael.guards.plugins.idea.Guardable;
import ch.raffael.guards.plugins.idea.PsiGuardUtil;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ComponentNotRegistered")
public class GuardsActionGroup extends AbstractGuardPopupGroup<PsiModifierListOwner> {

    public GuardsActionGroup(GuardPopupController controller, @Guardable final PsiModifierListOwner element) {
        super(controller, element);
        init(element);
    }

    public GuardsActionGroup(GuardPopupAction<?> parent, @Guardable final PsiModifierListOwner element) {
        super(parent, element);
        init(element);
    }

    protected void init(PsiModifierListOwner element) {
        setPopup(false);
        for( AnAction action : PsiGuardUtil.getGuards(element).transform(new Function<PsiAnnotation, AnAction>() {
            @Override
            public AnAction apply(PsiAnnotation psiAnnotation) {
                return new EditGuardActionGroup(GuardsActionGroup.this, psiAnnotation);
            }
        }) ) {
            add(action);
        }
    }

}
