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

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static ch.raffael.guards.plugins.idea.GuardsPlugin.DEBUG;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsInheritanceInspection extends AbstractBaseJavaLocalInspectionTool {

    @Nullable
    @Override
    public ProblemDescriptor[] checkMethod(@NotNull PsiMethod method, @NotNull InspectionManager manager, boolean isOnTheFly) {
        ProblemsHolder problems = new ProblemsHolder(InspectionManager.getInstance(method.getProject()), method.getContainingFile(), isOnTheFly);
        for( PsiAnnotation annotation : method.getModifierList().getAnnotations() ) {
            GuardInfo guardInfo = GuardInfo.forPsiAnnotation(annotation);
            if ( guardInfo.isGuard() ) {
                if ( DEBUG ) {
                    //manager.createProblemDescriptor()
                    //problems.registerProblem(annotation, "Guard: " + guardInfo, ProblemHighlightType.INFORMATION);
                }
                System.out.println(guardInfo);
            }
        }
        return problems.getResultsArray();
    }

}
