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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import com.google.common.collect.Iterables;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.ext.NullIf;
import ch.raffael.guards.plugins.idea.psi.PsiElementView;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class AbstractGuardPopupGroup<T extends PsiElementView> extends ActionGroup implements GuardPopupAction<T> {

    private final Support<T> guardPopup;

    private final List<AnAction> actions = new ArrayList<>();

    public AbstractGuardPopupGroup(@NotNull GuardPopupController controller, @Nullable T element) {
        guardPopup = new Support<>(this, controller, element);
    }

    public AbstractGuardPopupGroup(@NotNull GuardPopupController controller, @Nullable T element, SelectionKey<? extends T> selectionKey) {
        guardPopup = new Support<>(this, controller, element, selectionKey);
    }

    public AbstractGuardPopupGroup(@NotNull GuardPopupAction parent, @Nullable T element) {
        super();
        guardPopup = new Support<>(this, parent, element);
    }

    public AbstractGuardPopupGroup(@NotNull GuardPopupAction parent, @Nullable T element, SelectionKey<? extends T> selectionKey) {
        super();
        guardPopup = new Support<>(this, parent, element, selectionKey);
    }

    public void caption(@Nullable String text) {
        guardPopup.caption(text);
    }

    public void caption(String text, String description, Icon icon) {
        guardPopup.caption(text, description, icon);
    }

    public void caption(Icon icon) {
        guardPopup.caption(icon);
    }

    public void caption(String text, Icon icon) {
        guardPopup.caption(text, icon);
    }

    public void add(AnAction... actions) {
        this.actions.addAll(Arrays.asList(actions));
    }

    public void add(Collection<? extends AnAction> actions) {
        this.actions.addAll(actions);
    }

    public void add(Iterable<? extends AnAction> actions) {
        Iterables.addAll(this.actions, actions);
    }

    @NotNull
    @Override
    public AnAction[] getChildren(AnActionEvent e) {
        return actions.toArray(new AnAction[actions.size()]);
    }

    @Override
    @NullIf("Not selectable")
    public SelectionKey<? extends T> getSelectionKey() {
        return guardPopup.getSelectionKey();
    }

    @Override
    @ch.raffael.guards.NotNull
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
}
