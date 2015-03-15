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
import com.intellij.psi.PsiElement;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.ext.NullIf;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface GuardPopupAction<T extends PsiElement> {

    void setSelectable(boolean selectable);

    @NullIf("No target")
    T getSelectionElement();

    @NotNull
    GuardPopupController getController();

    @NullIf("No parent")
    GuardPopupAction<?> getParent();

    void extendKeyboardActions(KeyboardActionExtender extender);

    class Support<T extends PsiElement> implements GuardPopupAction<T> {

        private final AnAction action;
        private final GuardPopupController controller;
        private final GuardPopupAction<?> parent;
        private final T element;
        private boolean selectable = true;

        public Support(@NotNull AnAction action, @NotNull GuardPopupController controller, @Nullable T element) {
            this.action = action;
            this.controller = controller;
            this.parent = null;
            this.element = element;
        }

        public Support(@NotNull AnAction action, @NotNull GuardPopupAction<?> parent, @Nullable T element) {
            this.action = action;
            this.controller = parent.getController();
            this.parent = null;
            this.element = element;
        }

        public void caption(String text) {
            action.getTemplatePresentation().setText(text);
        }

        public void caption(Icon icon) {
            action.getTemplatePresentation().setIcon(icon);
        }

        public void caption(String text, Icon icon) {
            action.getTemplatePresentation().setText(text);
            action.getTemplatePresentation().setIcon(icon);
        }

        public void caption(String text, String description, Icon icon) {
            action.getTemplatePresentation().setText(text);
            action.getTemplatePresentation().setDescription(description);
            action.getTemplatePresentation().setIcon(icon);
        }

        @Override
        public void setSelectable(boolean selectable) {
            this.selectable = selectable;
        }

        @Override
        @NullIf("Not selectable")
        public T getSelectionElement() {
            if ( selectable ) {
                return element;
            }
            else {
                return null;
            }
        }

        @Override
        @NotNull
        public GuardPopupController getController() {
            return controller;
        }

        @Override
        @NullIf("root")
        public GuardPopupAction<?> getParent() {
            return parent;
        }

        public T getElement() {
            return element;
        }

        @Override
        public void extendKeyboardActions(KeyboardActionExtender extender) {
        }
    }

}
