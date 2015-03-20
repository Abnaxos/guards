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
import com.intellij.psi.PsiElement;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.ext.NullIf;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface GuardPopupAction<T extends PsiElement> {

    @NullIf("No target")
    SelectionKey<? extends T> getSelectionKey();

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
        private final SelectionKey<? extends T> selectionKey;

        public Support(@NotNull AnAction action, @NotNull GuardPopupController controller, @Nullable T element) {
            this(action, controller, element, defaultSelectionKey(element));
        }

        public Support(@NotNull AnAction action, @NotNull GuardPopupController controller, @Nullable T element, @NullIf("Not auto-selectable") SelectionKey<? extends T> selectionKey) {
            this.action = action;
            this.controller = controller;
            this.parent = null;
            this.element = element;
            this.selectionKey = selectionKey;
        }

        public Support(@NotNull AnAction action, @NotNull GuardPopupAction<?> parent, @Nullable T element) {
            this(action, parent, element, defaultSelectionKey(element));
        }

        public Support(@NotNull AnAction action, @NotNull GuardPopupAction<?> parent, @Nullable T element, @NullIf("Not auto-selectable") SelectionKey<? extends T> selectionKey) {
            this.action = action;
            this.controller = parent.getController();
            this.parent = null;
            this.element = element;
            this.selectionKey = selectionKey;
        }

        @NullIf("Input element is null")
        private static <T extends PsiElement> SelectionKey<? extends T> defaultSelectionKey(@Nullable T element) {
            if ( element == null ) {
                return null;
            }
            else {
                return SelectionKey.of(element);
            }
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
        @NullIf("Not selectable")
        public SelectionKey<? extends T> getSelectionKey() {
            return selectionKey;
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
