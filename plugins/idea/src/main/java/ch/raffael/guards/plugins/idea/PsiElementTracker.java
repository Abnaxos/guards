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

package ch.raffael.guards.plugins.idea;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretAdapter;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.markup.AttributesFlyweight;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.ui.JBColor;
import com.intellij.util.messages.MessageBusConnection;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class PsiElementTracker extends CaretAdapter implements FileEditorManagerListener, CaretListener {

    private final Project project;
    private final GuardsView guardsView;

    private Editor currentEditor;
    private MessageBusConnection msgBus;

    private PsiElement currentElement = null;
    private RangeHighlighter currentHighlight = null;

    public PsiElementTracker(Project project, GuardsView guardsView) {
        this.project = project;
        this.guardsView = guardsView;
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        Editor editor = event.getManager().getSelectedTextEditor();
        if ( editor != currentEditor ) {
            if ( currentEditor != null ) {
                currentEditor.getCaretModel().removeCaretListener(this);
            }
            currentEditor = editor;
            if ( currentEditor != null ) {
                currentEditor.getCaretModel().addCaretListener(this);
            }
            updateEditorElement();
        }
    }

    @Override
    public void caretPositionChanged(CaretEvent e) {
        updateEditorElement();
    }

    @Nullable
    private PsiElement elementUnderCursor() {
        if ( currentEditor == null ) {
            return null;
        }
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(currentEditor.getDocument());
        if ( psiFile == null ) {
            return null;
        }
        PsiElement element = null;
        element = psiFile.findElementAt(currentEditor.getCaretModel().getOffset());

        return element;
    }

    private void updateEditorElement() {
        if ( currentEditor == null ) {
            return;
        }
        if ( currentHighlight != null ) {
            currentEditor.getMarkupModel().removeHighlighter(currentHighlight);
            currentHighlight = null;
        }
        PsiGuardTarget target = new PsiGuardTarget(elementUnderCursor());
        if ( target.getVisual() != null ) {
            TextRange textRange = target.getVisual().getTextRange();
            currentHighlight = currentEditor.getMarkupModel().addRangeHighlighter(
                    textRange.getStartOffset(), textRange.getEndOffset(),
                    HighlighterLayer.ELEMENT_UNDER_CARET,
                    TextAttributes.fromFlyweight(AttributesFlyweight.create(null, null, 0, JBColor.YELLOW, EffectType.ROUNDED_BOX, null)),
                    HighlighterTargetArea.EXACT_RANGE);
        }
    }

    public void start() {
        msgBus = project.getMessageBus().connect();
        msgBus.subscribe(FILE_EDITOR_MANAGER, this);
        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
            @Override
            public void childAdded(@NotNull PsiTreeChangeEvent event) {
                update(event);
            }

            @Override
            public void childRemoved(@NotNull PsiTreeChangeEvent event) {
                update(event);
            }

            @Override
            public void childReplaced(@NotNull PsiTreeChangeEvent event) {
                update(event);
            }

            @Override
            public void childMoved(@NotNull PsiTreeChangeEvent event) {
                update(event);
            }

            @Override
            public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
                update(event);
            }

            public void update(@NotNull PsiTreeChangeEvent event) {
                updateEditorElement();
            }
        });
    }



}
