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

import javax.swing.KeyStroke;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiAnnotation;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.plugins.idea.PsiGuardUtil;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ComponentNotRegistered")
public class EditGuardActionGroup extends AbstractGuardPopupGroup<PsiAnnotation> {

    private EditGuardAction editAction;
    private DeleteGuardAction deleteAction;

    public EditGuardActionGroup(@NotNull GuardPopupController controller, @NotNull PsiAnnotation guard) {
        super(controller, guard);
        init(guard);
    }

    public EditGuardActionGroup(@NotNull GuardPopupAction parent, @NotNull PsiAnnotation guard) {
        super(parent, guard);
        init(guard);
    }

    private void init(PsiAnnotation guard) {
        caption(PsiGuardUtil.getGuardDescription(guard, false),
                PsiGuardUtil.getGuardDescription(guard, true),
                guard.getIcon(0));
        add(editAction = new EditGuardAction(this, guard));
        add(deleteAction = new DeleteGuardAction(this, guard));
        setPopup(true);
        //setShortcutSet(new CustomShortcutSet(KeyEvent.VK_DELETE));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        editAction.actionPerformed(e);
    }

    @Override
    public boolean canBePerformed(DataContext context) {
        return true;
    }

    @Override
    public void extendKeyboardActions(KeyboardActionExtender extender) {
        extender.addItemShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteAction);
    }
}
