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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiAnnotation;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ALL")
public class PushDownAction extends AbstractGuardPopupAction<PsiAnnotation> {

    public PushDownAction(GuardPopupController controller, PsiAnnotation element) {
        super(controller, element, SelectionKey.of(element, SelectionKey.Option.PULL_UP));
        caption("Push Down...", "Push the selected guard down the class hierarchy", AllIcons.Actions.MoveDown);
    }

    public PushDownAction(GuardPopupAction<?> parent, PsiAnnotation element) {
        super(parent, element, SelectionKey.of(element, SelectionKey.Option.PULL_UP));
        caption("Push Down...", "Push the selected guard down the class hierarchy", AllIcons.Actions.MoveDown);
    }

    @Override
    protected void perform(AnActionEvent e) {
    }
}
