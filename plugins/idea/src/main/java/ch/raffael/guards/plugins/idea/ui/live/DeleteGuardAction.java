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

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.plugins.idea.psi.PsiGuard;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
@SuppressWarnings("ComponentNotRegistered")
public class DeleteGuardAction extends AbstractGuardPopupWriteAction<PsiGuard> {

    public DeleteGuardAction(@Nullable GuardPopupController controller, @NotNull PsiGuard guard) {
        super(controller, guard, SelectionKey.of(guard, SelectionKey.Option.DELETE));
        init(guard);
    }

    public DeleteGuardAction(@Nullable GuardPopupAction<?> parent, @NotNull PsiGuard guard) {
        super(parent, guard, SelectionKey.of(guard, SelectionKey.Option.DELETE));
        init(guard);
    }

    protected void init(@NotNull PsiGuard guard) {
        caption("Delete", "Delete guard " + guard.getDescription(true), AllIcons.Actions.Delete);
        setShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)));
        getTemplatePresentation().setEnabled(guard.getElement().isWritable());
        setShortcutSet(new CustomShortcutSet(KeyEvent.VK_DELETE));
    }

    @Override
    public void perform(@NotNull AnActionEvent e) {
        getView().getElement().delete();
    }

}
