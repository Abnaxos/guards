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

import java.util.HashSet;
import java.util.Set;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;

import ch.raffael.guards.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class PsiGuardUtil {

    private PsiGuardUtil() {
    }

    public static boolean isGuarded(@NotNull PsiElement element) {
        // TODO: proper language check
        if ( !element.getLanguage().is(JavaLanguage.INSTANCE) ) {
            return false;
        }
        if ( element instanceof PsiClass ) {
            return isGuardAnnotation((PsiClass)element);
        }
        else if ( element instanceof PsiMethod ) {
            if ( isGuarded((PsiMethod)element) ) {
                return true;
            }
            for( PsiParameter param : ((PsiMethod)element).getParameterList().getParameters() ) {
                if ( isGuarded(param) ) {
                    return true;
                }
            }
        }
        else if (element instanceof PsiField ) {
            return isGuarded((PsiModifierListOwner)element);
        }
        else if ( element instanceof PsiParameter ) {
            return isGuarded((PsiParameter)element);
        }
        return false;
    }

    private static boolean isGuarded(@NotNull PsiModifierListOwner element) {
        if ( element.getModifierList() == null ) {
            return false;
        }
        for( PsiAnnotation annotation : element.getModifierList().getAnnotations() ) {
            if ( annotation.getNameReferenceElement() == null ) {
                continue;
            }
            PsiElement resolved = annotation.getNameReferenceElement().resolve();
            if ( !(resolved instanceof PsiClass) ) {
                continue;
            }
            if ( isGuardAnnotation((PsiClass)resolved) ) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGuardAnnotation(@NotNull PsiClass psiClass) {
        return isGuardAnnotation(new HashSet<PsiClass>(), psiClass);
    }

    public static boolean isGuardAnnotation(@NotNull Set<PsiClass> seen, @NotNull PsiClass psiClass) {
        if ( !seen.add(psiClass) ) {
            return false;
        }
        if ( !psiClass.isAnnotationType() ) {
            return false;
        }
        if ( psiClass.getModifierList() == null ) {
            return false;
        }
        if ( AnnotationUtil.isAnnotated(psiClass, ch.raffael.guards.definition.Guard.class.getName(), false) ) {
            return true;
        }
        for( PsiAnnotation annotation : psiClass.getModifierList().getAnnotations() ) {
            if ( annotation.getNameReferenceElement() == null ) {
                continue;
            }
            PsiElement resolved = annotation.getNameReferenceElement().resolve();
            if ( !(resolved instanceof PsiClass) ) {
                continue;
            }
            if ( isGuardAnnotation(seen, (PsiClass)resolved) ) {
                return true;
            }
        }
        return false;
    }

}
