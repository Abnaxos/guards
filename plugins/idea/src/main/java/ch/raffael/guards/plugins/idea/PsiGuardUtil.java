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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Contract;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.ext.NullIfNotFound;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class PsiGuardUtil {

    private static final FluentIterable<Object> EMPTY_FLUENT_ITERABLE = FluentIterable.from(Collections.emptyList());

    private PsiGuardUtil() {
    }

    @Contract("null -> false")
    public static boolean isGuardableLanguage(@Nullable Language language) {
        return JavaLanguage.INSTANCE.is(language);
    }

    @Contract("null -> false")
    public static boolean isGuardableLanguage(@Nullable PsiElement element) {
        return element != null && isGuardableLanguage(element.getLanguage());
    }

    @Contract("null -> false")
    public static boolean isGuardable(@Nullable PsiElement element) {
        return isGuardableLanguage(element) && (element instanceof PsiMethod || element instanceof PsiParameter);
    }

    @Contract("null -> false")
    public static boolean isGuardAnnotation(@Nullable PsiElement element) {
        PsiAnnotation annotation = as(PsiAnnotation.class, element);
        return annotation != null && isGuardType(resolve(annotation.getNameReferenceElement()));
    }


    static boolean isGuarded(@Nullable PsiElement element) {
        return getGuards(as(PsiModifierListOwner.class, element)).anyMatch(Predicates.alwaysTrue());
    }

    @NotNull
    static FluentIterable<PsiAnnotation> getGuards(@Nullable PsiModifierListOwner element) {
        if ( !isGuardable(element) || element.getModifierList() == null ) {
            return fluentIterable();
        }
        return fluentIterable(element.getModifierList().getAnnotations()).filter(new Predicate<PsiAnnotation>() {
            @Override
            public boolean apply(@Nullable PsiAnnotation psiElement) {
                return isGuardAnnotation(psiElement);
            }
        });
    }

    @Contract("null -> false")
    public static boolean isGuardType(@Nullable PsiElement element) {
        return isGuardableLanguage(element) && (element instanceof PsiClass)
                && isGuardType(new HashSet<PsiClass>(), (PsiClass)element);
    }

    public static boolean isGuardType(@NotNull Set<PsiClass> seen, @Nullable PsiClass psiClass) {
        if ( psiClass == null || !seen.add(psiClass) || !psiClass.isAnnotationType() ) {
            return false;
        }
        if ( psiClass.getModifierList() == null ) {
            return false;
        }
        if ( AnnotationUtil.isAnnotated(psiClass, ch.raffael.guards.definition.Guard.class.getName(), false) ) {
            return true;
        }
        for( PsiAnnotation annotation : psiClass.getModifierList().getAnnotations() ) {
            if ( annotation.getNameReferenceElement() != null ) {
                if ( isGuardType(seen, resolve(PsiClass.class, annotation.getNameReferenceElement())) ) {
                    return true;
                }
            }
        }
        return false;
    }

    @NullIfNotFound
    public static PsiElement resolve(@Nullable PsiElement element) {
        PsiReference ref = as(PsiReference.class, element);
        return ref == null ? null : ref.resolve();
    }

    @NullIfNotFound
    public static <T extends PsiElement> T resolve(@NotNull Class<T> expectedType, @Nullable PsiElement element) {
        return as(expectedType, resolve(element));
    }

    @Contract("null -> false")
    public static boolean isPrimitiveType(@Nullable PsiType element) {
        return element instanceof PsiPrimitiveType;
    }

    @Contract("null -> false")
    public static boolean isAnnotationType(@Nullable PsiClass element) {
        return element != null && element.isAnnotationType();
    }

    @NotNull
    public static <T> FluentIterable<T> fluentIterable(@Nullable Iterable<T> iterable) {
        if ( iterable == null || ((iterable instanceof Collection) && ((Collection)iterable).isEmpty()) ) {
            return fluentIterable();
        }
        else {
            return FluentIterable.from(iterable);
        }
    }

    @SafeVarargs
    @NotNull
    public static <T> FluentIterable<T> fluentIterable(@Nullable T... array) {
        if ( array == null || array.length == 0 ) {
            return fluentIterable();
        }
        else {
            return FluentIterable.from(Arrays.asList(array));
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> FluentIterable<T> fluentIterable() {
        return (FluentIterable<T>)EMPTY_FLUENT_ITERABLE;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T as(@NotNull Class<T> type, @Nullable Object object) {
        if ( type.isInstance(object) ) {
            return (T)object;
        }
        else {
            return null;
        }
    }

}
