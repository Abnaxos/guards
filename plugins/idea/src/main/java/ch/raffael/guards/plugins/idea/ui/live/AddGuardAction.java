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

import com.google.common.base.Predicate;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.plugins.idea.Diagnostics;
import ch.raffael.guards.plugins.idea.Guardable;
import ch.raffael.guards.plugins.idea.psi.PsiGuard;
import ch.raffael.guards.plugins.idea.psi.PsiGuardTarget;
import ch.raffael.guards.plugins.idea.psi.PsiGuardType;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ComponentNotRegistered")
public class AddGuardAction extends AbstractGuardPopupWriteAction<PsiGuardType> {

    private final PsiGuardTarget target;

    private int expandCount = 0;

    public AddGuardAction(GuardPopupController controller, @NotNull PsiGuardType guardType, @NotNull PsiGuardTarget target) {
        super(controller, guardType, SelectionKey.of(guardType, SelectionKey.Option.INSERT));
        this.target = target;
        caption(innerClassName(new StringBuilder(), guardType.getElement()).toString(), guardType.getDescription(), guardType.getElement().getIcon(0));
    }

    public AddGuardAction(GuardPopupAction parent, @NotNull PsiGuardType guardType, @NotNull @Guardable PsiGuardTarget target) {
        super(parent, guardType, SelectionKey.of(guardType, SelectionKey.Option.INSERT));
        this.target = target;
        caption(innerClassName(new StringBuilder(), guardType.getElement()).toString(), guardType.getDescription(), guardType.getElement().getIcon(0));
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
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(target.getElement().getProject());
        Document document = documentManager.getDocument(target.getElement().getContainingFile());
        if ( document == null ) {
            Diagnostics.notifyError(e, "Document is null");
            return;
        }
        if ( target.getElement().getModifierList() == null ) {
            Diagnostics.notifyError(e, "Modifier of list is null (Element: %s)", getView());
            return;
        }
        if ( getView().getElement().getQualifiedName() == null ) {
            Diagnostics.notifyError(e, "Anonymous guard type");
            return;
        }
        final PsiAnnotation psiAnnotation = target.getElement().getModifierList().addAnnotation(getView().getElement().getQualifiedName());
        JavaCodeStyleManager.getInstance(target.getElement().getProject()).shortenClassReferences(psiAnnotation);
        documentManager.doPostponedOperationsAndUnblockDocument(document);
        if ( getView().getAttributeMethods().iterator().hasNext() ) {
            new EditGuardAction(AddGuardAction.this,
                    target.getGuards().firstMatch(new Predicate<PsiGuard>() {
                        @Override
                        public boolean apply(PsiGuard psiGuard) {
                            return psiGuard.getElement().equals(psiAnnotation);
                        }
                    }).get()).actionPerformed(e);
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

    public PsiGuardTarget getTarget() {
        return target;
    }
}
