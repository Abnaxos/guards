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

import java.util.Set;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiSuperMethodUtil;
import com.intellij.psi.util.TypeConversionUtil;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
public class PsiTestMethod extends PsiElementView<PsiMethod, PsiHandlerClass> {

    private final PsiType psiType;

    PsiTestMethod(@NotNull PsiMethod psiMethod, @NotNull PsiHandlerClass psiHandlerClass) {
        super(psiMethod, psiHandlerClass);
        this.psiType = psiMethod.getParameterList().getParameters()[0].getType();
    }

    @NotNull
    public PsiMethod getPsiMethod() {
        return element;
    }

    @NotNull
    public PsiHandlerClass getPsiHandlerClass() {
        return getParent();
    }

    @NotNull
    public PsiType getPsiType() {
        return psiType;
    }

    public boolean isApplicableTo(@Nullable PsiType target) {
        if ( target == null || TypeConversionUtil.isVoidType(target) || TypeConversionUtil.isNullType(target) ) {
            return false;
        }
        // box the target type if necessary
        if ( !(psiType instanceof PsiPrimitiveType) && !(target instanceof PsiPrimitiveType) ) {
            return psiType.isAssignableFrom(target);
        }
        else if ( !(psiType instanceof PsiPrimitiveType) /* ->type must be non-primitive */ ) {
            // boxing
            PsiType boxed = ((PsiPrimitiveType)target).getBoxedType(element);
            if ( boxed == null ) {
                // sorry ...
                return false;
            }
            else {
                return psiType.isAssignableFrom(boxed);
            }
        }
        //noinspection ConstantConditions
        assert psiType instanceof PsiPrimitiveType;
        if ( !(target instanceof PsiPrimitiveType) ) {
            // unbox the target
            target = PsiPrimitiveType.getUnboxedType(target);
            if ( target == null ) {
                return false;
            }
        }
        if ( TypeConversionUtil.isFloatOrDoubleType(psiType) ^ TypeConversionUtil.isFloatOrDoubleType(target) ) {
            // the JLS allows widening integer types to float types; we don't
            return false;
        }
        // after we took our additional restriction out, we can use the standard method
        return TypeConversionUtil.areTypesConvertible(target, psiType);
    }

    public static boolean isTestMethod(@Nullable PsiMethod method, @Nullable Set<PsiMethod> seenMethods) {
        if ( method == null ) {
            return false;
        }
        if ( !method.hasModifierProperty(PsiModifier.PUBLIC) ) {
            return false;
        }
        if ( !PsiType.BOOLEAN.equals(method.getReturnType()) ) {
            return false;
        }
        if ( !"test".equals(method.getName()) && !AnnotationUtil.isAnnotated(method, Psi.C_TEST, false) ) {
            return false;
        }
        if ( method.getParameterList().getParameters().length != 1 ) {
            return false;
        }
        if ( seenMethods != null ) {
            for( PsiMethod s : seenMethods ) {
                if ( PsiSuperMethodUtil.isSuperMethod(s, method) ) {
                    return false;
                }
            }
            seenMethods.add(method);
        }
        return true;
    }

}
