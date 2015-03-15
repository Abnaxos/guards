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

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.KeyStroke;

import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.util.Processor;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.plugins.idea.Guardable;
import ch.raffael.guards.plugins.idea.PsiGuardUtil;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ComponentNotRegistered")
public class AddGuardActionGroup extends AbstractGuardPopupGroup<PsiModifierListOwner> {

    public AddGuardActionGroup(GuardPopupController controller, @NotNull @Guardable final PsiModifierListOwner element) {
        super(controller, element);
        init();
    }

    public AddGuardActionGroup(GuardPopupAction<?> parent, @NotNull @Guardable final PsiModifierListOwner element) {
        super(parent, element);
        init();
    }

    protected void init() {
        setPopup(true);
        setSelectable(false);
        getTemplatePresentation().setText("Add Guard...");
        getTemplatePresentation().setIcon(AllIcons.General.Add);
        if ( !(getElement() instanceof PsiMethod) || PsiGuardUtil.isGuardableReturnType((PsiMethod)getElement()) ) {
            final ArrayList<AddGuardAction> guardActions = new ArrayList<>();
            //Stopwatch sw = Stopwatch.createStarted();
            PsiGuardUtil.queryAllGuards(getElement()).forEach(new Processor<PsiClass>() {
                @Override
                public boolean process(@NotNull PsiClass psiClass) {
                    AddGuardAction action = new AddGuardAction(AddGuardActionGroup.this, getElement(), psiClass)
                    ;
                    guardActions.add(action);
                    // TODO: check for applicability
                    // TODO: check for duplicate names and try to expand them until unique
                    return true;
                }
            });
            //System.out.println(sw.stop() + " -> " + guardActions.size());
            Collections.sort(guardActions, new Comparator<AddGuardAction>() {
                @Override
                public int compare(@NotNull AddGuardAction left, @NotNull AddGuardAction right) {
                    return String.CASE_INSENSITIVE_ORDER.compare(left.getGuardType().getName(), right.getGuardType().getName());
                }
            });
            for( AddGuardAction action : guardActions ) {
                add(action);
            }
        }
        else {
            getTemplatePresentation().setEnabledAndVisible(false);
        }
    }
    
    @Override
    public boolean hideIfNoVisibleChildren() {
        return true;
    }

    @Override
    public void extendKeyboardActions(KeyboardActionExtender extender) {
        extender.addSelectShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), false);
        extender.addSelectShortcut(KeyStroke.getKeyStroke('+'), false);
    }
}
