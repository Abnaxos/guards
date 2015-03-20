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

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.util.containers.FilteringIterator;
import com.intellij.util.ui.JBSwingUtilities;

import ch.raffael.guards.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class ExtendedKeyboardActionDispatcher {

    private final Map<KeyStroke, Mapping> mappings = new HashMap<>();

    private final ListPopup popup;
    private final JList list;

    ExtendedKeyboardActionDispatcher(ListPopup popup) {
        this.popup = popup;
        Set<Component> lists = JBSwingUtilities.uiTraverser().preOrderTraversal(popup.getContent())
                .filter(new FilteringIterator.InstanceOf<>(JList.class)).toSet();
        if ( lists.size() != 1 ) {
            list = null;
        }
        else {
            list = (JList)lists.iterator().next();
        }
    }

    void install(JComponent component) {
        for( Object item : popup.getListStep().getValues() ) {
            if ( item instanceof PopupFactoryImpl.ActionItem ) {
                if ( ((PopupFactoryImpl.ActionItem)item).getAction() instanceof GuardPopupAction ) {
                    ((GuardPopupAction)((PopupFactoryImpl.ActionItem)item).getAction())
                            .extendKeyboardActions(new Extender((PopupFactoryImpl.ActionItem)item));
                }
            }
        }
        for( Map.Entry<KeyStroke, Mapping> mapping : mappings.entrySet() ) {
            mapping.getValue().registerCustomShortcutSet(new CustomShortcutSet(mapping.getKey()), component);
        }
    }

    private Mapping getMapping(KeyStroke keyStroke) {
        Mapping mapping = mappings.get(keyStroke);
        if ( mapping == null ) {
            mapping = new Mapping();
            mappings.put(keyStroke, mapping);
        }
        return mapping;
    }

    private final class Mapping extends AnAction {

        private final Map<PopupFactoryImpl.ActionItem, AnAction> whenSelected = new HashMap<>();
        private PopupFactoryImpl.ActionItem selectItem;
        private boolean selectHandleFinalChoices;

        @SuppressWarnings("SuspiciousMethodCalls")
        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            AnAction perform = whenSelected.get(list.getSelectedValue());
            if ( perform != null) {
                PopupFactoryImpl.ActionItem selected = (PopupFactoryImpl.ActionItem)list.getSelectedValue();
                if ( selected.isEnabled() && selected.equals(list.getSelectedValue()) ) {
                    Presentation presentation = perform.getTemplatePresentation().clone();
                    perform.update(event);
                    perform.beforeActionPerformedUpdate(event);
                    event = new AnActionEvent(
                            event.getInputEvent(), event.getDataContext(), event.getPlace(),
                            presentation, ActionManager.getInstance(), event.getModifiers());
                    if ( presentation.isEnabled() ) {
                        perform.actionPerformed(event);
                    }
                }
            }
            else if ( selectItem != null ) {
                if ( selectItem.isEnabled() ) {
                    list.setSelectedValue(selectItem, true);
                    popup.handleSelect(selectHandleFinalChoices);
                }
            }
        }
    }

    private final class Extender implements KeyboardActionExtender {

        private final PopupFactoryImpl.ActionItem actionItem;

        private Extender(PopupFactoryImpl.ActionItem actionItem) {
            this.actionItem = actionItem;
        }

        @Override
        public boolean addSelectShortcut(@NotNull KeyStroke keyStroke, boolean handleFinalChoices) {
            Mapping mapping = getMapping(keyStroke);
            if ( mapping.selectItem == null ) {
                mapping.selectItem = actionItem;
                mapping.selectHandleFinalChoices = handleFinalChoices;
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        public boolean addItemShortcut(@NotNull KeyStroke keyStroke, @NotNull final AnAction perform) {
            Mapping mapping = getMapping(keyStroke);
            if ( mapping.whenSelected.containsKey(actionItem) ) {
                return false;
            }
            mapping.whenSelected.put(actionItem, perform);
            return true;
        }
    }

}
