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

import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsLineMarkerProvider implements /*Annotator,*/ LineMarkerProvider {

    private static final Icon GUARDED_ICON = new ImageIcon(GuardsLineMarkerProvider.class.getResource("shield.png"));

    //@Override
    //public void annotate(@NotNull PsiElement element, AnnotationHolder holder) {
    //    //if ( element instanceof PsiMethod ) {
    //    //    PsiMethod method = (PsiMethod)element;
    //    //    for( PsiAnnotation annotation : method.getModifierList().getAnnotations() ) {
    //    //        GuardInfo guardInfo = GuardInfo.forPsiAnnotation(annotation);
    //    //        if ( guardInfo.isGuard() ) {
    //    //            if ( DEBUG ) {
    //    //                problems.registerProblem(annotation, "Guard: " + guardInfo, ProblemHighlightType.INFORMATION);
    //    //            }
    //    //            System.out.println(guardInfo);
    //    //        }
    //    //    }
    //    //}
    //
    //}

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if ( element instanceof PsiMethod ) {
            PsiMethod method = (PsiMethod)element;
            final List<GuardInfo> guardInfos = GuardInfo.forPsiMethod(method);
            if ( !guardInfos.isEmpty() ) {
                return new LineMarkerInfo<>(method, method.getNameIdentifier().getTextRange(), GUARDED_ICON, Pass.UPDATE_ALL,
                        new Function<PsiMethod, String>() {
                            @Override
                            public String fun(PsiMethod psiMethod) {
                                StringBuilder buf = new StringBuilder();
                                for( GuardInfo info : guardInfos ) {
                                    if ( buf.length() > 0 ) {
                                        buf.append('\n');
                                    }
                                    buf.append(info);
                                }
                                return buf.toString();
                            }
                        }, null, GutterIconRenderer.Alignment.RIGHT);
            }
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    }
}
