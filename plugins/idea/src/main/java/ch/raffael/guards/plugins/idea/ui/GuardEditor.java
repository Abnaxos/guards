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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.markup.AttributesFlyweight;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiParameter;
import com.intellij.ui.awt.RelativePoint;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.plugins.idea.GuardIcons;
import ch.raffael.guards.plugins.idea.model.GuardModelManager;
import ch.raffael.guards.plugins.idea.model.PsiGuardModel;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardEditor {

    private final JBPopupFactory popupFactory = JBPopupFactory.getInstance();

    private final Project project;
    private final Editor editor;
    private final Module module;
    private final GuardFocus focus;

    private final ListPopup popup;
    private final RelativePoint popupLocation;

    public GuardEditor(@NotNull final GuardFocus focus, @NotNull final Project project, @Nullable final Editor editor, @NotNull DataContext dataContext) {
        this.focus = focus;
        this.project = project;
        this.editor = editor;
        this.module = ModuleUtilCore.findModuleForPsiElement(focus.getElement());
        if ( editor != null ) {
            popup = popupFactory.createActionGroupPopup(null, buildElementGroup(focus.getElement()), dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);
            final TextRange elementRange = focus.visualRange(editor, project);
            assert elementRange != null;
            final RangeHighlighter highlight = editor.getMarkupModel().addRangeHighlighter(
                    elementRange.getStartOffset(), elementRange.getEndOffset(), HighlighterLayer.SELECTION,
                    TextAttributes.fromFlyweight(AttributesFlyweight.create(
                            editor.getColorsScheme().getColor(EditorColors.SELECTION_FOREGROUND_COLOR),
                            editor.getColorsScheme().getColor(EditorColors.SELECTION_BACKGROUND_COLOR),
                            0, null, null, null)),
                    HighlighterTargetArea.EXACT_RANGE);
            popup.addListener(new JBPopupListener.Adapter() {
                @Override
                public void onClosed(LightweightWindowEvent event) {
                    editor.getMarkupModel().removeHighlighter(highlight);
                }
            });
            VisualPosition visualPosition = editor.offsetToVisualPosition(highlight.getStartOffset());
            Point p = editor.visualPositionToXY(new VisualPosition(visualPosition.line + 1, visualPosition.column));
            final Rectangle visibleArea = editor.getScrollingModel().getVisibleArea();
            if ( !visibleArea.contains(p) ) {
                p = new Point((visibleArea.x + visibleArea.width) / 2, (visibleArea.y + visibleArea.height) / 2);
            }
            popupLocation = new RelativePoint(editor.getContentComponent(), p);
            new CycleGuardedElementAction(false).registerCustomShortcutSet(CustomShortcutSet.fromString("alt LEFT", "shift TAB", "PAGE_UP"), popup.getContent());
            new CycleGuardedElementAction(true).registerCustomShortcutSet(CustomShortcutSet.fromString("alt RIGHT", "TAB", "PAGE_DOWN"), popup.getContent());
        }
        else {
            popup = popupFactory.createActionGroupPopup(null, buildElementsGroup(), dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);
            popupLocation = popupFactory.guessBestPopupLocation(dataContext);
        }
        popup.addListener(new JBPopupListener.Adapter() {
            @Override
            public void onClosed(LightweightWindowEvent event) {
                Disposer.dispose(popup);
            }
        });
    }

    public static GuardEditor find(DataContext dataContext) {
        Project project = checkNotNull(CommonDataKeys.PROJECT.getData(dataContext), "Project is null");
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        PsiElement element = null;
        if ( editor != null ) {
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            if ( psiFile != null ) {
                element = psiFile.findElementAt(editor.getCaretModel().getOffset());
            }
            //if ( element == null ) {
            //    element = (PsiElement).getData(CommonDataKeys.PSI_ELEMENT.getName());
            //}
            if ( element == null ) {
                return null;
            }
        }
        else {
            element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        }
        if ( element == null ) {
            return null;
        }
        final GuardFocus focus = GuardFocus.find(element);
        if ( focus == null ) {
            return null;
        }
        return new GuardEditor(focus, project, editor, dataContext);
    }

    public void show() {
        popup.show(popupLocation);
    }

    private ActionGroup buildElementsGroup() {
        PsiClass psiClass = focus.getMethod().getContainingClass();
        SimpleActionGroup group;
        if ( psiClass != null ) {
            group = new SimpleActionGroup(psiClass.getQualifiedName(), psiClass.getIcon(0));
        }
        else {
            group = new SimpleActionGroup(focus.getMethod().getName(), focus.getMethod().getIcon(0));
        }
        group.add(buildElementGroup(focus.getMethod()));
        for( PsiParameter parameter : focus.getParameters() ) {
            group.add(buildElementGroup(parameter));
        }
        return group;
    }

    private ActionGroup buildElementGroup(PsiModifierListOwner element) {
        SimpleActionGroup group = new SimpleActionGroup(((PsiNamedElement)element).getName(), element.getIcon(0));
        group.add(buildAddGuardActions(element));
        group.add(new Separator());
        group.add(buildGuardsActions(element));
        return group.asPopup();
    }

    private ActionGroup buildAddGuardActions(PsiModifierListOwner element) {
        SimpleActionGroup addGroup = new SimpleActionGroup("Add Guard", AllIcons.General.Add);
        if ( module != null ) {
            for( PsiGuardModel model : GuardModelManager.get(module).getContext().findAllGuards() ) {
                addGroup.add(new AnAction("@" + model.getName(), null, model.getPsiClass().getIcon(0)) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        // FIXME: Not Implemented
                    }
                });
            }
        }
        //for( int i = 0; i < 3; i++ ) {
        //    addGroup.add(new AnAction("@AddGuard" + i, null, GuardIcons.Guard) {
        //        @Override
        //        public void actionPerformed(@NotNull AnActionEvent e) {
        //            // FIXME: implement this
        //        }
        //    });
        //}
        return addGroup.asPopup();
    }

    private List<ActionGroup> buildGuardsActions(PsiModifierListOwner element) {
        List<ActionGroup> result = new ArrayList<>();
        for( int i = 0; i < 4; i++ ) {
            final SimpleActionGroup guardsGroup = new SimpleActionGroup(
                    "@MyGuard" + i + (i == 2 ? "(foo, bar)" : ""), null, GuardIcons.Guard);
            guardsGroup.add(new AnAction("Edit", null, AllIcons.Actions.Edit) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    // FIXME: Not implemented
                }
            });
            final int index = i;
            guardsGroup.add(new AnAction("Delete", null, AllIcons.Actions.Delete) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    System.out.println("DELETE: " + index);
                }
            });
            result.add(guardsGroup.asPopup());
        }
        return result;
    }

    private class CycleGuardedElementAction extends AnAction {

        private final boolean forward;

        public CycleGuardedElementAction(boolean forward) {
            super();
            this.forward = forward;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            int newIndex = focus.getIndex() + (forward ? 1 : -1);
            if ( newIndex < -1 ) {
                newIndex = focus.getParameters().size() - 1;
            }
            else if ( newIndex >= focus.getParameters().size() ) {
                newIndex = -1;
            }
            GuardEditor ge = new GuardEditor(focus.forIndex(newIndex), project, editor, e.getDataContext());
            popup.cancel();
            ge.show();
        }
    }

}
