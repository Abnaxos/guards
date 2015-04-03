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

import javax.swing.JComponent;
import javax.swing.JFrame;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.util.PsiTreeUtil;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.plugins.idea.psi.Psi;
import ch.raffael.guards.plugins.idea.psi.PsiElementView;
import ch.raffael.guards.plugins.idea.psi.PsiGuard;
import ch.raffael.guards.plugins.idea.psi.PsiGuardTarget;
import ch.raffael.guards.plugins.idea.ui.GuardIcons;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class InvokeLiveEditorAction extends AnAction {

    public InvokeLiveEditorAction() {
        super("Edit Guards...", null, GuardIcons.EditGuardsAction);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if ( project == null ) {
            return;
        }
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if ( editor != null ) {
            showForEditor(project, editor, event.getDataContext());
        }
        else {
            PsiElement element = event.getData(CommonDataKeys.PSI_ELEMENT);
            PsiParameter parameter = null;
            PsiMember member = null;
            while ( element != null ) {
                if ( element instanceof PsiParameter ) {
                    parameter = (PsiParameter)element;
                }
                if ( element instanceof PsiMethod ) {
                    member = (PsiMember)element;
                    break;
                }
                element = element.getParent();
            }
            if ( member != null ) {
                Component component = event.getData(PlatformDataKeys.CONTEXT_COMPONENT);
                if ( !(component instanceof JComponent) ) {
                    component = null;
                    JFrame frame = WindowManager.getInstance().getFrame(project);
                    if ( frame != null ) {
                        component = frame.getRootPane();
                    }
                }
                if ( component != null ) {
                    GuardPopupController controller = new GuardPopupController((JComponent)component, member);
                    controller.shopPopup(event.getDataContext(), SelectionKey.of(PsiGuardTarget.get(parameter)));
                }
            }
        }
    }

    private boolean showForEditor(Project project, Editor editor, DataContext data) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        PsiFile file = psiDocumentManager.getPsiFile(editor.getDocument());
        if ( file == null ) {
            return false;
        }
        int offset = editor.getCaretModel().getCurrentCaret().getOffset();
        PsiElement origin = file.findElementAt(offset);
        if ( origin == null ) {
            return false;
        }
        // the deepest reference, if any (open editor on the parameter under cursor)
        PsiReferenceExpression currentReference = null;
        // the top annotation, if any (open editor on the guard under cursor)
        PsiAnnotation currentAnnotation = null;
        PsiMember member = null;
        do {
            if ( origin instanceof PsiAnnotation ) {
                currentAnnotation = (PsiAnnotation)origin;
            }
            if ( origin instanceof PsiReferenceExpression && currentReference == null ) {
                currentReference = (PsiReferenceExpression)origin;
            }
            else if ( origin instanceof PsiMethodCallExpression ) {
                PsiMethodCallExpression methodCall = (PsiMethodCallExpression)origin;
                member = methodCall.resolveMethod();
                break;
            }
            else if ( origin instanceof PsiMethod ) {
                member = (PsiMethod)origin;
                break;
            }
        } while ( (origin = origin.getParent()) != null );
        //if ( origin == null ) {
        //    return false;
        //}
        if ( member == null ) {
            return false;
        }
        GuardPopupController controller = new GuardPopupController(editor, member);
        PsiElementView initialSelection = null;
        if ( origin instanceof PsiMethod ) {
            // the cursor is on a method declaration
            //
            PsiMethod method = (PsiMethod)origin;
            // we have to investigate this further: were we in the method's signature or in
            // its body?
            PsiCodeBlock code = PsiTreeUtil.findChildOfType(method, PsiCodeBlock.class);
            if ( code != null && offset > code.getTextRange().getStartOffset() ) {
                // todo: WTF does this do?
                //if ( currentReference != null ) {
                //    // let's see, if we've got a parameter to preselect
                //    PsiElement element = currentReference.resolve();
                //    if ( element instanceof PsiParameter && !member.equals(((PsiParameter)(element)).getDeclarationScope()) ) {
                //        selected  = element;
                //        anchor = element.getTextRange();
                //    }
                //}
            }
            else {
                // find the parameter
                PsiParameterList parameterList = method.getParameterList();
                PsiParameter[] parameters = parameterList.getParameters();
                int paramIndex = Psi.findListIndex(offset, parameterList);
                if ( paramIndex >= 0 ) {
                    initialSelection = PsiGuardTarget.get(parameters[paramIndex]);
                }
                controller.extendMemberAnchor(method.getNameIdentifier());
                for( int i = 0; i < parameters.length; i++ ) {
                    PsiParameter parameter = parameters[i];
                    controller.extendParameterAnchor(i, parameter);
                }
            }
        }
        else {
            // the cursor is on a method call
            //
            assert origin instanceof PsiMethodCallExpression;
            PsiMethod targetMethod = (PsiMethod)member;
            PsiMethodCallExpression expression = (PsiMethodCallExpression)origin;
            int paramIndex = Psi.findListIndex(offset, expression.getArgumentList());
            if ( paramIndex >= 0 ) {
                PsiParameter[] parameters = targetMethod.getParameterList().getParameters();
                if ( paramIndex > parameters.length ) {
                    if ( parameters.length > 0 && parameters[parameters.length - 1].isVarArgs() ) {
                        initialSelection = PsiGuardTarget.get(parameters[parameters.length - 1]);
                    }
                }
            }
            controller.extendMemberAnchor(expression.getMethodExpression().getReferenceNameElement());
            PsiExpression[] expressions = expression.getArgumentList().getExpressions();
            for( int i = 0; i < expressions.length; i++ ) {
                controller.extendParameterAnchor(i, expressions[i]);
            }
        }
        if ( currentAnnotation != null ) {
            if ( initialSelection == null || PsiTreeUtil.isAncestor(initialSelection.getElement(), currentAnnotation, false) ) {
                initialSelection = PsiGuard.of(currentAnnotation);
            }
        }
        controller.shopPopup(data, SelectionKey.of(initialSelection));
        return true;
    }

}
