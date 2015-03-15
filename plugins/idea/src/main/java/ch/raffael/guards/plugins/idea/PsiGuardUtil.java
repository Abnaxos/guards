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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiAnnotationMethod;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.DirectClassInheritorsSearch;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.EmptyQuery;
import com.intellij.util.FilteredQuery;
import com.intellij.util.Query;
import org.jetbrains.annotations.Contract;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.Unsigned;
import ch.raffael.guards.definition.Message;
import ch.raffael.guards.ext.InstanceOf;
import ch.raffael.guards.ext.NullIf;
import ch.raffael.guards.ext.NullIfNotFound;
import ch.raffael.guards.ext.UnsignedOrNotFound;


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


    public static boolean isGuarded(@Nullable PsiElement element) {
        return getGuards(as(PsiModifierListOwner.class, element)).anyMatch(Predicates.alwaysTrue());
    }

    @NotNull
    public static FluentIterable<PsiAnnotation> getGuards(@Nullable PsiModifierListOwner element) {
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

    @Nullable
    public static PsiClass getGuardAnnotationType(@Nullable PsiAnnotation annotation) {
        if ( annotation == null ) {
            return null;
        }
        if ( annotation.getNameReferenceElement() == null ) {
            return null;
        }
        PsiClass type = as(PsiClass.class, annotation.getNameReferenceElement().resolve());
        return isGuardType(new HashSet<PsiClass>(), type) ? type : null;
    }

    @NotNull
    public static FluentIterable<PsiAnnotationMethod> getGuardAnnotationMethods(@Nullable PsiClass guardType) {
        if ( isGuardType(guardType) ) {
            return fluentIterable(guardType.getMethods())
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
        else {
            return fluentIterable();
        }
    }

    @NotNull
    public static Query<PsiClass> queryAllGuards(@NotNull PsiElement origin) {
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
    public static Query<PsiClass> queryAllGuards(@NotNull GlobalSearchScope scope, @NotNull Project project) {
        PsiClass annotationClass = JavaPsiFacade.getInstance(project).findClass("java.lang.annotation.Annotation", scope);
        if ( annotationClass == null ) {
            return EmptyQuery.getEmptyQuery();
        }
        return new FilteredQuery<>(
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
                });
        // this one takes ages ... :(
        //return new FilteredQuery<>(
        //        AllClassesSearch.search(scope, project),
        //        new Condition<PsiClass>() {
        //            @Override
        //            public boolean value(PsiClass psiClass) {
        //                return psiClass.isAnnotationType() && isGuardType(new HashSet<PsiClass>(), psiClass);
        //            }
        //        }
        //
        //);
    }

    @NotNull
    public static String getGuardDescription(@NotNull PsiAnnotation guard, boolean full) {
        if ( !isGuardAnnotation(guard) ) {
            return guard.getText();
        }
        StringBuilder buf = new StringBuilder();
        PsiJavaCodeReferenceElement ref = guard.getNameReferenceElement();
        if ( ref == null ) {
            buf.append('@').append(guard.getQualifiedName());
        }
        else {
            buf.append('@').append(ref.getReferenceName());
        }
        if ( full && guard.getParameterList().getAttributes().length > 0 ) {
            buf.append('(');
            boolean first = true;
            for( PsiNameValuePair value : guard.getParameterList().getAttributes() ) {
                if ( first ) {
                    first = false;
                }
                else {
                    buf.append(", ");
                }
                buf.append(value.getName()).append("=");
                buf.append(value.getValue() == null ? "?" : value.getValue().getText());
            }
            buf.append(')');
        }
        return buf.toString();
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

    public static boolean isGuardableReturnType(@Nullable PsiMethod element) {
        return element != null && isGuardableReturnType((element).getReturnType());
    }

    public static boolean isGuardableReturnType(@Nullable PsiType type) {
        return type != null && !type.equals(PsiType.VOID);
    }

    @NullIf("No description available")
    public static String getGuardTypeDescription(@Nullable PsiClass element) {
        if ( element == null ) {
            return null;
        }
        final PsiClass messageType = JavaPsiFacade.getInstance(element.getProject()).findClass(Message.class.getName(), element.getResolveScope());
        if ( messageType != null && messageType.isAnnotationType() ) {
            PsiAnnotation message = PsiGuardUtil.fluentIterable(AnnotationUtil.getAllAnnotations(element, false, null))
                    .firstMatch(new Predicate<PsiAnnotation>() {
                        @Override
                        public boolean apply(@Nullable PsiAnnotation psiAnnotation) {
                            return psiAnnotation != null
                                    && Message.class.getName().equals(psiAnnotation.getQualifiedName());
                        }
                    }).orNull();
            if ( message != null ) {
                PsiAnnotationMemberValue value = message.findAttributeValue("value");
                if ( value != null ) {
                    Object eval = JavaPsiFacade.getInstance(message.getProject()).getConstantEvaluationHelper().computeConstantExpression(value);
                    if ( eval != null ) {
                        return eval.toString();
                    }
                    //return value.getText();
                }
            }
        }
        StringBuilder allAnnotations = new StringBuilder();
        Joiner.on(" ").skipNulls().appendTo(allAnnotations,
                getGuards(element).transform(new Function<PsiAnnotation, String>() {
                    @Override
                    public String apply(@Nullable PsiAnnotation psiAnnotation) {
                        if ( psiAnnotation == null ) {
                            return null;
                        }
                        return psiAnnotation.getText();
                    }
                }));
        if ( allAnnotations.length() != 0 ) {
            return allAnnotations.toString();
        }
        return element.getQualifiedName();
    }

    @UnsignedOrNotFound
    public static int findListIndex(@Unsigned int offset,
                             @InstanceOf({PsiParameterList.class, PsiExpressionList.class})
                             @Nullable PsiElement listElement)
    {
        if ( listElement == null ) {
            return -1;
        }
        int index = -1;
        for( PsiElement element : listElement.getChildren() ) {
            if ( element.getTextRange().contains(offset) ) {
                return index;
            }
            if ( PsiUtil.isJavaToken(element, JavaTokenType.LPARENTH) || PsiUtil.isJavaToken(element, JavaTokenType.COMMA) ) {
                index++;
            }
            if ( PsiUtil.isJavaToken(element, JavaTokenType.RPARENTH) ) {
                return -1;
            }
        }
        return - 1;
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
