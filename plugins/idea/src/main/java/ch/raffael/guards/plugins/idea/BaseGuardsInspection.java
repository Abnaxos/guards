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

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import ch.raffael.guards.definition.Guard;

import static ch.raffael.guards.plugins.idea.GuardInfo.noGuard;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class BaseGuardsInspection extends BaseJavaLocalInspectionTool {

    @NotNull
    protected JavaPsiFacade javaPsi(@NotNull PsiElement element) {
        return JavaPsiFacade.getInstance(element.getProject());
    }

    @NotNull
    protected GuardInfo getGuardInfo(@NotNull PsiAnnotation annotation) {
        GuardInfo guardInfo = annotation.getUserData(GuardInfo.key());
        if ( guardInfo == null ) {
            if ( annotation.getQualifiedName() == null ) {
                return noGuard(annotation);
            }
            PsiClass annotationClass = javaPsi(annotation).findClass(annotation.getQualifiedName(), annotation.getResolveScope());
            if ( annotationClass == null ) {
                return noGuard(annotation);
            }
            guardInfo = annotationClass.getUserData(GuardInfo.key());
            if ( guardInfo == null ) {
                if ( annotationClass.getModifierList() == null ) {
                    return noGuard(annotationClass);
                }
                for( PsiAnnotation metaAnnotation : annotationClass.getModifierList().getAnnotations() ) {
                    if ( Guard.class.getName().equals(metaAnnotation.getQualifiedName()) ) {
                        guardInfo = new PsiGuardInfo(annotationClass);
                        annotation.putUserData(GuardInfo.key(), guardInfo);
                    }
                }
            }
        }
        if ( guardInfo == null ) {
            guardInfo = noGuard();
        }
        return guardInfo;
    }

}
