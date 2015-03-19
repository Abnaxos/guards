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
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.plugins.idea.Guardable;
import ch.raffael.guards.plugins.idea.Notifications;
import ch.raffael.guards.plugins.idea.PsiGuardUtil;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ComponentNotRegistered")
public class AddGuardAction extends AbstractGuardPopupWriteAction<PsiModifierListOwner> {

    private final PsiClass guardType;

    private int expandCount = 0;

    public AddGuardAction(GuardPopupController controller, @NotNull PsiModifierListOwner element, @NotNull PsiClass guardType) {
        super(controller, element, SelectionKey.of(guardType, SelectionKey.Option.INSERT));
        this.guardType = guardType;
        caption(innerClassName(new StringBuilder(), guardType).toString(), PsiGuardUtil.getGuardTypeDescription(guardType), guardType.getIcon(0));
    }

    public AddGuardAction(GuardPopupAction parent, @NotNull @Guardable PsiModifierListOwner element, @NotNull PsiClass guardType) {
        super(parent, element, SelectionKey.of(guardType, SelectionKey.Option.INSERT));
        this.guardType = guardType;
        caption(innerClassName(new StringBuilder(), guardType).toString(), PsiGuardUtil.getGuardTypeDescription(guardType), guardType.getIcon(0));
    }

    @NotNull
    private static StringBuilder innerClassName(@NotNull StringBuilder buf, @NotNull PsiClass type) {
        if ( type.getContainingClass() != null ) {
            innerClassName(buf, type.getContainingClass());
        }
        if ( buf.length() != 0 ) {
            buf.append('.');
        }
        buf.append(type.getName());
        return buf;
    }

    @Override
    public void perform(@NotNull final AnActionEvent e) {
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(getElement().getProject());
        Document document = documentManager.getDocument(getElement().getContainingFile());
        if ( document == null ) {
            Notifications.notifyError("fkjs");
            return;
        }
        if ( getElement().getModifierList() == null ) {
            Notifications.notifyError("fsa");
            return;
        }
        if ( guardType.getQualifiedName() == null ) {
            Notifications.notifyError("fsa");
            return;
        }
        PsiAnnotation psiAnnotation = getElement().getModifierList().addAnnotation(guardType.getQualifiedName());
        JavaCodeStyleManager.getInstance(getElement().getProject()).shortenClassReferences(psiAnnotation);
        documentManager.doPostponedOperationsAndUnblockDocument(document);
        if ( PsiGuardUtil.getGuardAnnotationMethods(guardType).iterator().hasNext() ) {
            new EditGuardAction(AddGuardAction.this, psiAnnotation).actionPerformed(e);
        }
        //PsiDocumentManager.getInstance(getElement().getProject()).commitDocument();
        //new EditGuardAction()
        setShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0)));
        //getController().shopPopup(e.getDataContext(), SelectionKey.of(psiAnnotation, SelectionKey.Option.PULL_UP));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // FIXME: Not implemented
        super.update(e);
    }

    public PsiClass getGuardType() {
        return guardType;
    }

}
