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

package ch.raffael.guards.plugins.idea.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Icon;

import com.google.common.collect.Iterables;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import ch.raffael.guards.NoNulls;
import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.Positive;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class SimpleActionGroup extends ActionGroup implements Iterable<AnAction> {

    private final List<AnAction> actions = new ArrayList<>();

    public SimpleActionGroup(@Nullable String name, @Nullable String description, @Nullable Icon icon) {
        super(name, description, icon);
    }

    public SimpleActionGroup(@Nullable String name, @Nullable Icon icon) {
        super(name, null, icon);
    }

    public SimpleActionGroup() {
        super();
    }

    public SimpleActionGroup asPopup() {
        setPopup(true);
        return this;
    }

    public SimpleActionGroup add(@NotNull @NoNulls AnAction... actions) {
        this.actions.addAll(Arrays.asList(actions));
        return this;
    }

    public SimpleActionGroup add(@NotNull @NoNulls Iterable<? extends AnAction> actions) {
        Iterables.addAll(this.actions, actions);
        return this;
    }

    @NotNull
    @Override
    public AnAction[] getChildren(AnActionEvent e) {
        return actions.toArray(new AnAction[actions.size()]);
    }

    @NotNull
    public AnAction getChild(@Positive int index) {
        return actions.get(index);
    }

    @Positive
    public int getChildCount() {
        return actions.size();
    }

    @NotNull
    public List<AnAction> asList() {
        return actions;
    }

    @Override
    @NotNull
    public ListIterator<AnAction> iterator() {
        return actions.listIterator();
    }

    @NotNull
    public ListIterator<AnAction> iterator(@Positive int startIndex) {
        return actions.listIterator(startIndex);
    }
}
