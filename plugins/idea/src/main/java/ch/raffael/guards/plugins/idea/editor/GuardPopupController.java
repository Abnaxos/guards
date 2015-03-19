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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.markup.AttributesFlyweight;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.ui.popup.WizardPopup;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.containers.FilteringIterator;
import com.intellij.util.ui.JBSwingUtilities;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.Unsigned;
import ch.raffael.guards.plugins.idea.ElementIndex;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardPopupController implements Disposable {

    private final static ConcurrentMap<Component, Boolean> INSTALLED =
            new MapMaker().weakKeys().concurrencyLevel(1).makeMap();

    private final JBPopupFactory popupFactory = JBPopupFactory.getInstance();

    private final JComponent component;
    private final RelativePoint position;
    private final Editor editor;

    private final PsiMember member;
    private final List<PsiParameter> parameters;

    private TextRange memberAnchor;
    private final TextRange[] parameterAnchors;

    @ElementIndex
    private Integer popupIndex = null;
    private final LinkedList<JBPopup> popups = new LinkedList<>();

    private SelectionKey<?> selection;

    public GuardPopupController(JComponent component, PsiMember member) {
        this(null, component, null, member);
    }

    public GuardPopupController(RelativePoint position, JComponent component, PsiMember member) {
        this(position, component, null, member);
    }

    public GuardPopupController(Editor editor, PsiMember member) {
        this(null, editor.getComponent(), editor, member);
    }

    private GuardPopupController(RelativePoint position, JComponent component, Editor editor, PsiMember member) {
        this.position = position;
        this.component = component;
        this.editor = editor;
        this.member = member;
        if ( member instanceof PsiMethod ) {
            parameters = ImmutableList.copyOf(((PsiMethod)member).getParameterList().getParameters());
        }
        else {
            parameters = ImmutableList.of();
        }
        parameterAnchors = new TextRange[parameters.size()];
    }

    @NotNull
    public Project getProject() {
        return member.getProject();
    }

    public PsiMember getMember() {
        return member;
    }

    public List<PsiParameter> getParameters() {
        return parameters;
    }

    public void extendMemberAnchor(@Nullable PsiElement element) {
        extendMemberAnchor(textRange(element));
    }

    public void extendMemberAnchor(@Nullable TextRange range) {
        if ( range == null ) {
            return;
        }
        memberAnchor = union(memberAnchor, range);
    }

    public void extendParameterAnchor(@Unsigned int index, @Nullable PsiElement element) {
        extendParameterAnchor(index, textRange(element));
    }

    public void extendParameterAnchor(@Unsigned int index, @Nullable TextRange range) {
        if ( range == null ) {
            return;
        }
        parameterAnchors[index] = union(parameterAnchors[index], range);
    }

    public JBPopup shopPopup(DataContext data) {
        return shopPopup(data, null);
    }

    public JBPopup shopPopup(DataContext data, SelectionKey<?> select) {
        int offset = -1;
        boolean selectInline = false;
        if ( editor != null ) {
            selectInline = memberAnchor != null;
            for( TextRange paramHighlight : parameterAnchors ) {
                if ( paramHighlight == null ) {
                    selectInline = false;
                    break;
                }
            }
        }
        selection = select;
        ActionGroup actionGroup;
        if ( !selectInline ) {
            if ( !parameters.isEmpty() ) {
                actionGroup = new MemberParametersActionGroup(this, member, parameters);
            }
            else {
                actionGroup = new ElementActionGroup(this, member);
            }
        }
        else {
            if ( selection != null ) {
                PsiElement param = PsiTreeUtil.getParentOfType(selection.getElement(), PsiParameter.class, false);
                if ( param != null ) {
                    //noinspection SuspiciousMethodCalls
                    popupIndex = parameters.indexOf(param);
                }
            }
            if ( popupIndex == null ) {
                popupIndex = -1;
            }
            actionGroup = new ElementActionGroup(this, popupIndex < 0 ? member : parameters.get(popupIndex));
        }
        //final JBPopup popup = popupFactory.createActionGroupPopup(null, actionGroup,
        //        data, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);
        JBPopup popup = createPopup(data, actionGroup);
        if ( selectInline) {
            final TextRange anchor = popupIndex < 0 ? memberAnchor : parameterAnchors[popupIndex];
            popup.addListener(new JBPopupListener() {
                public RangeHighlighter highlight = null;
                @Override
                public void beforeShown(LightweightWindowEvent event) {
                    highlight = editor.getMarkupModel().addRangeHighlighter(
                            anchor.getStartOffset(), anchor.getEndOffset(), HighlighterLayer.SELECTION,
                            TextAttributes.fromFlyweight(AttributesFlyweight.create(
                                    editor.getColorsScheme().getColor(EditorColors.SELECTION_FOREGROUND_COLOR),
                                    editor.getColorsScheme().getColor(EditorColors.SELECTION_BACKGROUND_COLOR),
                                    0, null, null, null)),
                            HighlighterTargetArea.EXACT_RANGE);
                }
                @Override
                public void onClosed(LightweightWindowEvent event) {
                    if ( highlight != null ) {
                        editor.getMarkupModel().removeHighlighter(highlight);
                    }
                    popups.remove(event.asPopup());
                    event.asPopup().dispose();
                }
            });
            offset = anchor.getStartOffset();
            new CycleElementAction(true).registerCustomShortcutSet(KeyEvent.VK_TAB, 0, popup.getContent());
            new CycleElementAction(true).registerCustomShortcutSet(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK, popup.getContent());
        }
        if ( editor != null && offset < 0 ) {
            offset = editor.getCaretModel().getOffset();
        }
        return showPopup(data, popup, offset);
        //}
        //else {
        //    return null;
        //}
    }

    protected JBPopup createPopup(DataContext data, final ActionGroup actionGroup) {
        disposePopups();
        final ListPopup popup = new RootPopup(
                null, actionGroup, data, false, false, true, false, null, -1, null, null) {
            @Override
            protected void initPopup(ListPopup popup) {
                if ( selection != null ) {
                    popup.addListener(new JBPopupListener() {
                        @Override
                        public void beforeShown(LightweightWindowEvent event) {
                            final ListPopup listPopup = (ListPopup)event.asPopup();
                            //listPopup.removeListener(this);
                            preselect(listPopup);
                        }

                        @Override
                        public void onClosed(LightweightWindowEvent event) {
                        }
                    });
                }
                if ( INSTALLED.putIfAbsent(popup.getContent(), true) != null ) {
                    return;
                }
                ExtendedKeyboardActionDispatcher dispatcher = new ExtendedKeyboardActionDispatcher(popup);
                dispatcher.install(popup.getContent());
            }
        };
        return popup;
    }

    private <T extends JBPopup> T showPopup(DataContext data, T popup, int offset) {
        if ( editor != null ) {
            VisualPosition visualPosition = editor.offsetToVisualPosition(offset);
            Point p = editor.visualPositionToXY(new VisualPosition(visualPosition.line + 1, visualPosition.column));
            final Rectangle visibleArea = editor.getScrollingModel().getVisibleArea();
            if ( !visibleArea.contains(p) ) {
                p = new Point((visibleArea.x + visibleArea.width) / 2, (visibleArea.y + visibleArea.height) / 2);
            }
            popups.add(popup);
            popup.show(new RelativePoint(editor.getContentComponent(), p));
            //((ListPopup)popup).handleSelect(false);
        }
        else if ( position != null ) {
            popup.show(position);
        }
        else {
            popup.show(popupFactory.guessBestPopupLocation(component));
        }
        return popup;
    }

    private void preselect(final ListPopup popup) {
        Iterator items = popup.getListStep().getValues().iterator();
        int index = preselectedIndex(items);
        if ( index >= 0 ) {
            findJList(popup).setSelectedIndex(index);
            AnAction action = ((PopupFactoryImpl.ActionItem)popup.getListStep().getValues().get(index)).getAction();
            if ( action instanceof ActionGroup ) {
                if ( preselectedIndex(Arrays.asList(((ActionGroup)action).getChildren(null)).iterator()) >= 0 ) {
                    // ensure that the next sub-menu gets activated
                    Window window = (Window)SwingUtilities.getRoot(popup.getContent());
                    if ( window.isVisible() ) {
                        popup.handleSelect(false);
                    }
                    else {
                        window.addWindowListener(
                                new WindowAdapter() {
                                    @Override
                                    public void windowOpened(WindowEvent e) {
                                        popup.handleSelect(false);
                                        e.getWindow().removeWindowListener(this);
                                    }
                                });
                    }
                }
            }
        }
    }

    private int preselectedIndex(Iterator items) {
        for( int index = 0; items.hasNext(); index++ ) {
            Object item = items.next();
            GuardPopupAction<?> action = null;
            if ( item instanceof GuardPopupAction ) {
                action = (GuardPopupAction)item;
            }
            else if ( item instanceof PopupFactoryImpl.ActionItem && ((PopupFactoryImpl.ActionItem)item).getAction() instanceof GuardPopupAction ) {
                action= (GuardPopupAction)((PopupFactoryImpl.ActionItem)item).getAction();
            }
            if ( action == null ) {
                continue;
            }
            if ( action.getSelectionKey() != null && action.getSelectionKey().isSelectableBy(selection) ) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public void dispose() {
        disposePopups();
    }

    private void disposePopups() {
        while ( !popups.isEmpty() ) {
            JBPopup popup = popups.removeLast();
            popup.dispose();
        }
    }

    private DataContext getDataContext() {
        return DataManager.getInstance().getDataContext(editor.getContentComponent());
    }

    @Nullable
    private static TextRange textRange(@Nullable PsiElement element) {
        return element == null ? null : element.getTextRange();
    }

    @Nullable
    private static TextRange union(@Nullable TextRange currentRange, @Nullable TextRange newRange) {
        if ( currentRange == null ) {
            return newRange;
        }
        else if ( newRange != null ) {
            return currentRange.union(newRange);
        }
        else {
            return null;
        }
    }

    static JList findJList(ListPopup popup) {
        Set<Component> lists = JBSwingUtilities.uiTraverser().preOrderTraversal(popup.getContent())
                .filter(new FilteringIterator.InstanceOf<>(JList.class)).toSet();
        if ( lists.size() != 1 ) {
            throw new IllegalStateException("Expected exactly one JList");
        }
        return (JList)lists.iterator().next();
    }

    private class CycleElementAction extends AnAction {
        private final boolean forward;
        public CycleElementAction(boolean forward) {
            this.forward = forward;
        }
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if ( popupIndex == null ) {
                return;
            }
            int prevAnchor = popupIndex;
            popupIndex += forward ? 1 : -1;
            if ( popupIndex < -1 ) {
                popupIndex = parameters.size() - 1;
            }
            else if ( popupIndex >= parameters.size() ) {
                popupIndex = -1;
            }
            if ( prevAnchor != popupIndex ) {
                shopPopup(getDataContext());
            }
        }
    }

    /**
     * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
     */
    abstract class RootPopup extends PopupFactoryImpl.ActionGroupPopup {

        RootPopup(String title, ActionGroup actionGroup, DataContext dataContext, boolean showNumbers, boolean useAlphaAsNumbers, boolean showDisabledActions, boolean honorActionMnemonics, Runnable disposeCallback, int maxRowCount, Condition<AnAction> preselectActionCondition, String actionPlace) {
            super(title, actionGroup, dataContext, showNumbers, useAlphaAsNumbers, showDisabledActions, honorActionMnemonics, disposeCallback, maxRowCount, preselectActionCondition, actionPlace);
            initPopup(this);
        }

        protected abstract void initPopup(ListPopup popup);

        @Override
        protected WizardPopup createPopup(WizardPopup parent, PopupStep step, Object parentValue) {
            ChildPopup popup = new ChildPopup(parent, (ListPopupStep)step, parentValue);
            initPopup(popup);
            return popup;
        }

        @Override
        protected void afterShow() {
        }

        class ChildPopup extends ListPopupImpl {
            ChildPopup(WizardPopup aParent, ListPopupStep aStep, Object parentValue) {
                super(aParent, aStep, parentValue);
            }
            @Override
            protected WizardPopup createPopup(WizardPopup parent, PopupStep step, Object parentValue) {
                return RootPopup.this.createPopup(parent, step, parentValue);
            }
            @Override
            protected void afterShow() {
            }
        }

    }
}
