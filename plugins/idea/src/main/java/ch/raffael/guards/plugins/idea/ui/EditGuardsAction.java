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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import ch.raffael.guards.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class EditGuardsAction extends AnAction {

    //public void actionPerformed(@NotNull AnActionEvent e) {
    //    System.out.println("COMPONENT: " + e.getData(CommonDataKeys.PROJECT).getComponent(GuardsPluginProject.class));
    //    boolean found = false;
    //    PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
    //    System.out.println("ELEMENT: " + element);
    //    System.out.println("EDITOR: " + e.getData(CommonDataKeys.EDITOR));
    //    System.out.println("HOST_EDITOR: " + e.getData(CommonDataKeys.HOST_EDITOR));
    //    if ( element == null ) {
    //        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
    //        Caret caret = e.getData(CommonDataKeys.CARET);
    //        if ( file != null && caret != null ) {
    //            element = file.findElementAt(caret.getOffset());
    //        }
    //    }
    //    if ( element != null ) {
    //        PsiMethod method = findMethod(element);
    //        if ( method != null ) {
    //            for( GuardInfo info : GuardInfo.forPsiMethod(method) ) {
    //                found = true;
    //                System.out.println("GUARD: " + info);
    //            }
    //        }
    //    }
    //    Project project = getEventProject(e);
    //    if ( project != null ) {
    //        Component component = WindowManagerEx.getInstanceEx().getFocusedComponent(project);
    //        System.out.println(component);
    //    }
    //    if ( !found ) {
    //        System.out.println("NO GUARDS FOUND");
    //    }
    //}
    //
    //private PsiMethod findMethod(PsiElement elem) {
    //    if ( elem == null ) {
    //        return null;
    //    }
    //    if ( elem instanceof PsiMethod ) {
    //        return (PsiMethod)elem;
    //    }
    //    else {
    //        return findMethod(elem.getParent());
    //    }
    //}


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if ( project == null ) {
            return;
        }
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiElement element = null;
        if ( editor != null ) {
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            if ( psiFile != null ) {
                element = psiFile.findElementAt(editor.getCaretModel().getOffset());
            }
        }
        if ( element == null) {
            element = e.getData(CommonDataKeys.PSI_ELEMENT);
        }
        if ( element == null ) {
            return;
        }
        final GuardFocus focus = GuardFocus.find(element);
        if ( focus == null ) {
            return;
        }
        GuardEditor guardEditor = GuardEditor.find(e.getDataContext());
        if ( guardEditor == null ) {
            return;
        }
        guardEditor.show();
    }
}
