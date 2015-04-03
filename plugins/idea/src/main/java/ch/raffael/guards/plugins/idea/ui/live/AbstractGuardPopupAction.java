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

import javax.swing.Icon;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.ext.NullIf;
import ch.raffael.guards.plugins.idea.psi.PsiElementView;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class AbstractGuardPopupAction<T extends PsiElementView> extends AnAction implements GuardPopupAction<T> {

    private final Support<T> guardPopup;

    protected AbstractGuardPopupAction(GuardPopupController controller, T element) {
        guardPopup = new Support<T>(this, controller, element, SelectionKey.of(element));
    }

    protected AbstractGuardPopupAction(GuardPopupController controller, T element, SelectionKey<? extends T> selectionKey) {
        guardPopup = new Support<>(this, controller, element, selectionKey);
    }

    protected AbstractGuardPopupAction(GuardPopupAction<?> parent, T element) {
        guardPopup = new Support<>(this, parent, element);
    }

    protected AbstractGuardPopupAction(GuardPopupAction<?> parent, T element, SelectionKey<? extends T> selectionKey) {
        guardPopup = new Support<>(this, parent, element, selectionKey);
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
    @NullIf("Not selectable")
    public SelectionKey<? extends T> getSelectionKey() {
        return guardPopup.getSelectionKey();
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

    public T getView() {
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
