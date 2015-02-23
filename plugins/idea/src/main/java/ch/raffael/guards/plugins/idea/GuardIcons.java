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

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.ide.IconLayerProvider;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import org.jetbrains.annotations.Nullable;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.definition.Guard;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class GuardIcons implements IconLayerProvider {

    private static Icon load(String resPath) {
        return new ImageIcon(GuardIcons.class.getResource(resPath));
    }

    public static final Icon Guard = load("guards.png");
    public static final Icon GuardLayer = load("guard-layer.png");

    @Nullable
    @Override
    public Icon getLayerIcon(@NotNull Iconable element, boolean isLocked) {
        // TODO: proper language check
        if ( !(element instanceof PsiElement) || !((PsiElement)element).getLanguage().is(JavaLanguage.INSTANCE) ) {
            return null;
        }
        if ( element instanceof PsiClass ) {
            PsiClass psiClass = (PsiClass)element;
            if ( psiClass.isAnnotationType() && AnnotationUtil.isAnnotated(psiClass, Guard.class.getName(), false) ) {
                return GuardLayer;
            }
        }
        else if ( element instanceof PsiMethod ) {
            if ( isGuarded((PsiModifierListOwner)element) ) {
                return GuardLayer;
            }
            for( PsiParameter param : ((PsiMethod)element).getParameterList().getParameters() ) {
                if ( isGuarded(param) ) {
                    return GuardLayer;
                }
            }
        }
        else if ( element instanceof PsiParameter ) {
            if ( isGuarded((PsiParameter)element) ) {
                return GuardLayer;
            }
        }
        return null;
    }

    private boolean isGuarded(PsiModifierListOwner element) {
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
            if ( isGuardAnnoation((PsiClass)resolved) ) {
                return true;
            }
        }
        return false;
    }

    private boolean isGuardAnnoation(PsiClass psiClass) {
        return AnnotationUtil.isAnnotated(psiClass, ch.raffael.guards.definition.Guard.class.getName(), false);
    }

    @Nullable
    @Override
    public String getLayerDescription() {
        return "Layer for guard annotations";
    }
}
