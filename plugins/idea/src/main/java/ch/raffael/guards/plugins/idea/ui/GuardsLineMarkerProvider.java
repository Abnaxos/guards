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

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;

import com.google.common.base.Predicate;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.ide.DataManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.ui.awt.RelativePoint;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.plugins.idea.psi.Psi;
import ch.raffael.guards.plugins.idea.ui.live.GuardPopupController;
import ch.raffael.guards.plugins.idea.util.NullSafe;

import static ch.raffael.guards.plugins.idea.psi.Psi.isAnnotationType;
import static ch.raffael.guards.plugins.idea.psi.Psi.isGuarded;
import static ch.raffael.guards.plugins.idea.psi.Psi.isPrimitiveType;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsLineMarkerProvider implements /*Annotator,*/ LineMarkerProvider {


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
        if ( !Psi.isGuardableLanguage(element) ) {
            return null;
        }
        if ( element instanceof PsiMethod ) {
            final PsiMethod method = (PsiMethod)element;
            int startOffset;
            if ( method.getNameIdentifier() == null ) {
                startOffset = method.getTextRange().getStartOffset();
            }
            else {
                startOffset = method.getNameIdentifier().getTextRange().getStartOffset();
            }
            Icon icon = null;
            if ( !isAnnotationType(method.getContainingClass()) ) {
                boolean fullyGuarded = method.getReturnType() == null
                        || isPrimitiveType(method.getReturnType())
                        || isGuarded(method);
                if ( fullyGuarded ) {
                    fullyGuarded = !NullSafe.fluentIterable(method.getParameterList().getParameters())
                            .filter(new Predicate<PsiParameter>() {
                                // remove all unknown / primitive types
                                @Override
                                public boolean apply(@Nullable PsiParameter psiParameter) {
                                    return psiParameter != null && !isPrimitiveType(psiParameter.getType());
                                }
                            })
                            .anyMatch(new Predicate<PsiParameter>() {
                                // true if there is any unguarded
                                @Override
                                public boolean apply(@Nullable PsiParameter psiParameter) {
                                    return !isGuarded(psiParameter);
                                }
                            });
                }
                if ( fullyGuarded ) {
                    icon = GuardIcons.GutterGuard;
                }
                else {
                    icon = GuardIcons.GutterGuardWarning;
                }
            }
            return new LineMarkerInfo<>(method, startOffset, icon, Pass.UPDATE_ALL,
                    null /*new Function<PsiMethod, String>() {
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
                    }*/,
                    new GutterIconNavigationHandler<PsiMethod>() {
                        @Override
                        public void navigate(MouseEvent e, PsiMethod method) {
                            new GuardPopupController(
                                    new RelativePoint(e), (JComponent)e.getComponent(), method)
                                    .shopPopup(DataManager.getInstance().getDataContext(e.getComponent()));
                            // TODO: open popup with mouse
                            //GuardFocus focus = GuardFocus.find(method);
                            //if ( focus == null ) {
                            //    return;
                            //}
                            //new GuardEditor(focus, method.getProject(), null, DataManager.getInstance().getDataContextFromFocus().getResult())
                            //        .show(RelativePoint.fromScreen(e.getLocationOnScreen()));
                        }
                    }, GutterIconRenderer.Alignment.RIGHT);
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    }
}
