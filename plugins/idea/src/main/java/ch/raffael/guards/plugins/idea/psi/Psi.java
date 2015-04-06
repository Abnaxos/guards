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

import java.lang.annotation.Annotation;

import com.google.common.collect.FluentIterable;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.Contract;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.Unsigned;
import ch.raffael.guards.definition.Guard;
import ch.raffael.guards.ext.InstanceOf;
import ch.raffael.guards.ext.NullIfNotFound;
import ch.raffael.guards.ext.UnsignedOrNotFound;

import static ch.raffael.guards.plugins.idea.util.NullSafe.cast;
import static ch.raffael.guards.plugins.idea.util.NullSafe.fluentIterable;
import static java.util.Arrays.asList;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Psi {

    public static final String C_GUARD = Guard.class.getCanonicalName();
    public static final String C_HANDLER = Guard.Handler.class.getCanonicalName();
    public static final String M_GUARD_HANDLER = "handler";
    public static final String C_TEST = HandlerAccess.C_TEST;

    private Psi() {
    }

    @Contract("null -> false")
    public static boolean isGuardableLanguage(@Nullable PsiElement element) {
        return element != null && JavaLanguage.INSTANCE.is(element.getLanguage());
    }


    @NotNull
    public static FluentIterable<PsiAnnotation> getDeclaredAnnotations(@Nullable PsiModifierListOwner owner) {
        if ( owner == null ) {
            return fluentIterable();
        }
        if ( owner.getModifierList() != null ) {
            return fluentIterable(owner.getModifierList().getAnnotations());
        }
        else {
            return fluentIterable();
        }
    }

    @NullIfNotFound
    public static PsiElement resolve(@Nullable PsiElement element) {
        PsiReference ref = cast(PsiReference.class, element);
        return ref == null ? null : ref.resolve();
    }

    @NullIfNotFound
    public static <T extends PsiElement> T resolve(@NotNull Class<T> expectedType, @Nullable PsiElement element) {
        return cast(expectedType, resolve(element));
    }

    @Contract("null -> false")
    public static boolean isPrimitiveType(@Nullable PsiType element) {
        return element instanceof PsiPrimitiveType;
    }

    @Contract("null -> false")
    public static boolean isAnnotationType(@Nullable PsiClass element) {
        return element != null && element.isAnnotationType();
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

    @Nullable
    public static PsiClass findClassByName(@Nullable PsiElement element, String name) {
        if ( element == null ) {
            return null;
        }
        return JavaPsiFacade.getInstance(element.getProject()).findClass(name, element.getResolveScope());
    }


    public static boolean isSuperClass(@Nullable PsiClass superClass, @Nullable PsiClass psiClass) {
        if ( superClass == null || psiClass == null ) {
            return false;
        }
        return asList(psiClass.getSupers()).contains(superClass);
    }

    public static boolean isSuperClass(@Nullable PsiClass superClass, @Nullable PsiType type) {
        return isSuperClass(superClass, PsiTypesUtil.getPsiClass(type));
    }

    public static boolean isSuperClass(@NotNull String name, @Nullable PsiClass psiClass) {
        return isSuperClass(findClassByName(psiClass, name), psiClass);
    }

    public static boolean isSuperClass(@NotNull String name, @Nullable PsiType type) {
        return isSuperClass(name, PsiTypesUtil.getPsiClass(type));
    }

    private static class HandlerAccess extends Guard.Handler<Annotation> {

        private static final String C_TEST = Test.class.getName();
    }

}
