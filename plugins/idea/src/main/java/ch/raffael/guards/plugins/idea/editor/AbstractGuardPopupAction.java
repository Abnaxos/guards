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

import javax.swing.Icon;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.ext.NullIf;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class AbstractGuardPopupAction<T extends PsiElement> extends AnAction implements GuardPopupAction<T> {

    private final Support<T> guardPopup;

    protected AbstractGuardPopupAction(GuardPopupController controller, T element) {
        guardPopup = new Support<>(this, controller, element);
    }

    protected AbstractGuardPopupAction(GuardPopupAction<?> parent, T element) {
        guardPopup = new Support<>(this, parent, element);
    }

    public void caption(String text) {
        guardPopup.caption(text);
    }

    public void caption(Icon icon) {
        guardPopup.caption(icon);
    }

    public void caption(String text, Icon icon) {
        guardPopup.caption(text, icon);
    }

    public void caption(String text, String description, Icon icon) {
        guardPopup.caption(text, description, icon);
    }

    @Override
    public void setSelectable(boolean selectable) {
        guardPopup.setSelectable(selectable);
    }

    @Override
    @NullIf("Not selectable")
    public T getSelectionElement() {
        return guardPopup.getSelectionElement();
    }

    @Override
    @NotNull
    public GuardPopupController getController() {
        return guardPopup.getController();
    }

    @Override
    @NullIf("root")
    public GuardPopupAction<?> getParent() {
        return guardPopup.getParent();
    }

    public T getElement() {
        return guardPopup.getElement();
    }

    @Override
    public void extendKeyboardActions(KeyboardActionExtender extender) {
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        perform(e);
    }

    protected abstract void perform(AnActionEvent e);

}
