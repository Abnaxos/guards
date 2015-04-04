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

package ch.raffael.guards.plugins.idea.psi;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.FluentIterable;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMethod;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassObjectAccessExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.DirectClassInheritorsSearch;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.FilteredQuery;
import org.jetbrains.annotations.Contract;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.definition.Message;
import ch.raffael.guards.ext.NullIf;
import ch.raffael.guards.plugins.idea.util.InspectionQueue;

import static ch.raffael.guards.plugins.idea.psi.Psi.C_GUARD;
import static ch.raffael.guards.plugins.idea.psi.Psi.C_HANDLER;
import static ch.raffael.guards.plugins.idea.psi.Psi.M_GUARD_HANDLER;
import static ch.raffael.guards.plugins.idea.psi.Psi.findClassByName;
import static ch.raffael.guards.plugins.idea.psi.Psi.isGuardableLanguage;
import static ch.raffael.guards.plugins.idea.psi.Psi.isSuperClass;
import static ch.raffael.guards.plugins.idea.psi.Psi.resolve;
import static ch.raffael.guards.plugins.idea.util.NullSafe.cast;
import static ch.raffael.guards.plugins.idea.util.NullSafe.fluentIterable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("SimplifiableIfStatement")
public class PsiGuardType extends PsiElementView<PsiClass, PsiElementView> {

    PsiGuardType(PsiClass element) {
        super(element, null);
    }

    @NullIf("Not a guard class or invalid")
    public static PsiGuardType ofPsiClass(@Nullable PsiClass psiClass) {
        if ( psiClass == null ) {
            return null;
        }
        if ( !isGuardableLanguage(psiClass) ) {
            return null;
        }
        if ( !isGuardType(psiClass) ) {
            return null;
        }
        return new PsiGuardType(psiClass);
    }

    @NullIf("Not a guard annotation")
    public static PsiGuardType ofPsiAnnotation(@Nullable PsiAnnotation annotation) {
        if ( annotation == null ) {
            return null;
        }
        return ofPsiClass(cast(PsiClass.class, resolve(annotation.getNameReferenceElement())));
    }

    @NotNull
    public static FluentIterable<PsiGuardType> queryAllGuards(@NotNull PsiElement origin) {
        // TODO: this shows classes from other modules; can't figure out why and how to suppress this
        //GlobalSearchScope scope = origin.getResolveScope();
        //PsiFile file = origin.getContainingFile();
        //if ( file != null ) {
        //    Module module = FileIndexFacade.getInstance(origin.getProject()).getModuleForFile(file.getVirtualFile());
        //    if ( module != null ) {
        //        scope = module.getModuleWithDependenciesAndLibrariesScope(false);
        //    }
        //}
        return queryAllGuards(origin.getResolveScope(), origin.getProject());
    }

    @NotNull
    public static FluentIterable<PsiGuardType> queryAllGuards(@NotNull GlobalSearchScope scope, @NotNull Project project) {
        PsiClass annotationClass = JavaPsiFacade.getInstance(project).findClass("java.lang.annotation.Annotation", scope);
        if ( annotationClass == null ) {
            return fluentIterable();
        }
        return fluentIterable(new FilteredQuery<>(
                new FilteredQuery<>(
                        DirectClassInheritorsSearch.search(annotationClass, scope, false, false),
                        new Condition<PsiClass>() {
                            private final Set<String> seen = new HashSet<>();

                            @Override
                            public boolean value(PsiClass psiClass) {
                                return psiClass.getQualifiedName() != null && seen.add(psiClass.getQualifiedName());
                            }
                        }),
                new Condition<PsiClass>() {
                    @Override
                    public boolean value(PsiClass psiClass) {
                        return isGuardType(new HashSet<PsiClass>(), psiClass);
                    }
                }))
                .transform(new Function<PsiClass, PsiGuardType>() {
                    @Override
                    public PsiGuardType apply(@Nullable PsiClass psiClass) {
                        return psiClass == null ? null : new PsiGuardType(psiClass);
                    }
                })
                .filter(Predicates.notNull());
    }

    public static boolean isGuardType(@Nullable PsiClass psiClass) {
        if ( psiClass == null ) {
            return false;
        }
        if ( !isGuardableLanguage(psiClass) ) {
            return false;
        }
        return isGuardType(new HashSet<PsiClass>(), psiClass);
    }

    private static boolean isGuardType(@NotNull Set<PsiClass> seen, @Nullable PsiClass psiClass) {
        if ( psiClass == null || !seen.add(psiClass) || !psiClass.isAnnotationType() ) {
            return false;
        }
        if ( psiClass.getModifierList() == null ) {
            return false;
        }
        if ( AnnotationUtil.isAnnotated(psiClass, C_GUARD, false) ) {
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

    @Contract("null -> false")
    public static boolean isGuardAnnotation(@Nullable PsiAnnotation annotation) {
        return annotation != null && isGuardType(cast(PsiClass.class, resolve(annotation.getNameReferenceElement())));
    }

    public FluentIterable<PsiAnnotationMethod> getAttributeMethods() {
        return fluentIterable(element.getMethods())
                .transform(new Function<PsiMethod, PsiAnnotationMethod>() {
                    @Override
                    public PsiAnnotationMethod apply(@Nullable PsiMethod psiMethod) {
                        if ( psiMethod instanceof PsiAnnotationMethod ) {
                            return (PsiAnnotationMethod)psiMethod;
                        }
                        else {
                            return null;
                        }
                    }
                })
                .filter(Predicates.notNull());
    }

    @Nullable
    public PsiHandlerClass getDirectHandlerClass() {
        PsiAnnotation annotation = AnnotationUtil.findAnnotation(element, C_GUARD);
        if ( annotation == null ) {
            return null;
        }
        for( PsiNameValuePair attribute : annotation.getParameterList().getAttributes() ) {
            if ( M_GUARD_HANDLER.equals(attribute.getName()) ) {
                PsiClassObjectAccessExpression value = cast(PsiClassObjectAccessExpression.class, attribute.getValue());
                if ( value != null ) {
                    if ( isSuperClass(C_HANDLER, value.getOperand().getType()) ) {
                        PsiClass handlerClass = PsiTypesUtil.getPsiClass(value.getOperand().getType());
                        if ( handlerClass != null ) {
                            return new PsiHandlerClass(handlerClass, this);
                        }
                    }
                }
            }
        }
        PsiClass handlerClass = getElement().findInnerClassByName("Handler", false);
        if ( handlerClass != null ) {
            if ( isSuperClass(C_HANDLER, handlerClass) ) {
                return new PsiHandlerClass(handlerClass, this);
            }
        }
        handlerClass = findClassByName(getElement(), getElement().getQualifiedName() + "GuardHandler");
        if ( handlerClass != null ) {
            if ( isSuperClass(C_HANDLER, handlerClass) ) {
                return new PsiHandlerClass(handlerClass, this);
            }
        }
        return null;
    }

    @NotNull
    public FluentIterable<PsiGuardType> getAllGuardTypes() {
        final InspectionQueue<PsiGuardType> queue = new InspectionQueue<>(this);
        return fluentIterable(new Iterable<PsiGuardType>() {
            @Override
            public Iterator<PsiGuardType> iterator() {
                return new AbstractIterator<PsiGuardType>() {
                    @Override
                    protected PsiGuardType computeNext() {
                        PsiGuardType type = queue.poll();
                        if ( type == null ) {
                            return endOfData();
                        }
                        for( PsiAnnotation annotation : Psi.getDeclaredAnnotations(type.element) ) {
                            queue.add(ofPsiAnnotation(annotation));
                        }
                        return type;
                    }
                };
            }
        });
    }

    @NotNull
    public FluentIterable<PsiHandlerClass> getHandlerClasses() {
        return getAllGuardTypes()
                .transform(new Function<PsiGuardType, PsiHandlerClass>() {
                    @Override
                    public PsiHandlerClass apply(@Nullable PsiGuardType psiGuardType) {
                        return psiGuardType == null ? null : psiGuardType.getDirectHandlerClass();
                    }
                })
                .filter(Predicates.notNull());
    }

    public boolean isApplicableTo(@Nullable final PsiType type) {
        if ( type == null || TypeConversionUtil.isVoidType(type) || TypeConversionUtil.isNullType(type) ) {
            return false;
        }
        if ( getDirectHandlerClass() != null ) {
            if ( !getDirectHandlerClass().getTestMethods().anyMatch(new Predicate<PsiTestMethod>() {
                @Override
                public boolean apply(PsiTestMethod psiTestMethod) {
                    return psiTestMethod.isApplicableTo(type);
                }
            }) )
            {
                return false;
            }
        }
        for( PsiHandlerClass handlerClass : getHandlerClasses() ) {
            if ( !handlerClass.getTestMethods().anyMatch(
                    new Predicate<PsiTestMethod>() {
                        @Override
                        public boolean apply(@Nullable PsiTestMethod psiTestMethod) {
                            return psiTestMethod != null && psiTestMethod.isApplicableTo(type);
                        }}) )
            {
                return false;
            }
        }
        return true;
    }

    public String getDescription() {
        final PsiClass messageType = Psi.findClassByName(element, Message.class.getName());
        if ( messageType != null && messageType.isAnnotationType() ) {
            PsiAnnotation message = fluentIterable(Psi.getDeclaredAnnotations(element))
                    .firstMatch(new Predicate<PsiAnnotation>() {
                        @Override
                        public boolean apply(@Nullable PsiAnnotation psiAnnotation) {
                            return psiAnnotation != null
                                    && Message.class.getName().equals(psiAnnotation.getQualifiedName());
                        }
                    }).orNull();
            if ( message != null ) {
                return AnnotationUtil.getStringAttributeValue(message, "value");
            }
        }
        StringBuilder allAnnotations = new StringBuilder();
        Joiner.on(" ").skipNulls().appendTo(allAnnotations,
                Psi.getGuards(element).transform(new Function<PsiGuard, String>() {
                    @Override
                    public String apply(@Nullable PsiGuard psiAnnotation) {
                        if ( psiAnnotation == null ) {
                            return null;
                        }
                        return psiAnnotation.getElement().getText();
                    }
                }));
        if ( allAnnotations.length() != 0 ) {
            return allAnnotations.toString();
        }
        return element.getQualifiedName();
    }

}
