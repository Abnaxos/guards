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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ActiveIcon;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.Consumer;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.plugins.idea.GuardsApplicationComponent;
import ch.raffael.guards.plugins.idea.psi.PsiGuard;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
@SuppressWarnings("ComponentNotRegistered")
public class EditGuardAction extends AbstractGuardPopupWriteAction<PsiGuard> {

    private static final Pattern EMPTY_ARGS_RE = Pattern.compile("\\s*\\(\\s*\\)(\\s*)");

    private AnAction onFinish;

    public EditGuardAction(@Nullable GuardPopupController controller, @NotNull PsiGuard guard) {
        super(controller, guard, SelectionKey.of(guard, SelectionKey.Option.EDIT));
        init(guard);
    }

    public EditGuardAction(@Nullable GuardPopupAction<?> parent, @NotNull PsiGuard guard) {
        super(parent, guard, SelectionKey.of(guard, SelectionKey.Option.EDIT));
        init(guard);
    }

    protected void init(@NotNull PsiGuard guard) {
        caption("Edit...", "Delete guard " + guard.getDescription(true), AllIcons.Actions.Edit);
        //setShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)));
        getTemplatePresentation().setEnabled(guard.getElement().isWritable());
    }

    public EditGuardAction onFinish(AnAction anAction) {
        onFinish = anAction;
        return this;
    }

    @Override
    public void perform(@NotNull AnActionEvent event) {
        showEditorPopup(event);
    }

    protected void showEditorPopup(@NotNull AnActionEvent event) {
        final Project project = getController().getProject();
        final PsiFile psiFile = getView().getElement().getContainingFile();
        if ( psiFile == null ) {
            return;
        }
        final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if ( document == null ) {
            return;
        }
        final Editor editor = EditorFactory.getInstance().createEditor(document, project, psiFile.getVirtualFile(), false);
        final JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(ScrollPaneFactory.createScrollPane(editor.getComponent()), editor.getContentComponent())
                .setProject(project)
                .setTitle(psiFile.getName())
                .setTitleIcon(new ActiveIcon(psiFile.getIcon(0)))
                .setRequestFocus(true)
                .setMovable(true)
                .setResizable(true)
                .setShowBorder(true)
                .setCancelKeyEnabled(false)
                .setCancelOnClickOutside(true)
                .setShowBorder(true)
                .setShowShadow(true)
                .createPopup();
        popup.addListener(new JBPopupListener.Adapter() {
            @Override
            public void onClosed(LightweightWindowEvent event) {
                GuardsApplicationComponent.getUiState().setPopupWidth(popup.getContent().getWidth());
                GuardsApplicationComponent.getUiState().setPopupHeight(popup.getContent().getHeight());
                EditorFactory.getInstance().releaseEditor(editor);
                popup.dispose();
            }
        });
        popup.setSize(new Dimension(
                GuardsApplicationComponent.getUiState().getPopupWidth(),
                GuardsApplicationComponent.getUiState().getPopupHeight()));
        popup.show(JBPopupFactory.getInstance().guessBestPopupLocation(event.getDataContext()));
        // init editor
        //WriteCommandAction.runWriteCommandAction(project, new Runnable() {
        //    @Override
        //    public void run() {
                String text = getView().getElement().getParameterList().getText();
                TextRange range = getView().getElement().getParameterList().getTextRange();
                final boolean handleParens;
                if ( text.startsWith("(") && text.endsWith(")") ) {
                    handleParens = true;
                    text = text.substring(1, text.length() - 1);
                }
                else if ( text.trim().isEmpty() ) {
                    handleParens = true;
                }
                else {
                    handleParens = false;
                }
                editor.getCaretModel().moveToOffset(range.getStartOffset());
                editor.getDocument().deleteString(range.getStartOffset(), range.getEndOffset());
                TemplateManager templateManager = TemplateManager.getInstance(project);
                Template template = templateManager.createTemplate("", "");
                template.setToReformat(true);
                String expression = "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
                if ( handleParens ) {
                    template.addTextSegment("(");
                }
                template.addVariable("params", expression, expression, true);
                template.addVariableSegment("params");
                if ( handleParens ) {
                    template.addTextSegment(")");
                }
                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                editor.getSelectionModel().setSelection(range.getStartOffset(), range.getEndOffset());
                templateManager.startTemplate(editor, template, new TemplateEditingAdapter() {
                    private TextRange argsRange = null;

                    @Override
                    public void templateFinished(Template template, boolean brokenOff) {
                        if ( brokenOff ) {
                            popup.cancel();
                        }
                        else {
                            removeEmptyParens();
                            popup.closeOk(null);
                            DataManager.getInstance().getDataContextFromFocus().doWhenDone(new Consumer<DataContext>() {
                                @Override
                                public void consume(final DataContext dataContext) {
                                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            getController().shopPopup(dataContext, SelectionKey.of(getView(), SelectionKey.Option.PULL_UP));
                                        }
                                    });
                                }
                            });
                        }
                    }

                    private void removeEmptyParens() {
                        if ( !handleParens || argsRange == null ) {
                            return;
                        }
                        if ( argsRange.getStartOffset() <= 0 ) {
                            return;
                        }
                        argsRange = argsRange.shiftRight(-1).grown(1);
                        TextRange allRange = TextRange.from(0, document.getTextLength());
                        if ( !allRange.contains(argsRange) ) {
                            return;
                        }
                        String docText = document.getText();
                        if ( docText.charAt(argsRange.getStartOffset()) != '(' ) {
                            return;
                        }
                        if ( docText.charAt(argsRange.getEndOffset()) != ')' ) {
                            return;
                        }
                        if ( !docText.substring(argsRange.getStartOffset() + 1, argsRange.getEndOffset()).trim().isEmpty() ) {
                            return;
                        }
                        document.deleteString(argsRange.getStartOffset(), argsRange.getEndOffset() + 1);
                    }

                    @Override
                    public void currentVariableChanged(TemplateState templateState, Template template, int oldIndex, int newIndex) {
                        if ( templateState.getCurrentVariableNumber() == 0 ) {
                            argsRange = templateState.getCurrentVariableRange();
                        }
                    }

                    @Override
                    public void templateCancelled(Template template) {
                        editor.getContentComponent().registerKeyboardAction(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                popup.cancel();
                            }
                        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
                    }
                });
            //}
        //});
    }

}
