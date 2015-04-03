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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Contract;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.ext.NullIf;
import ch.raffael.guards.plugins.idea.util.NullSafe;

import static ch.raffael.guards.plugins.idea.util.NullSafe.fluentIterable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class PsiGuardTarget extends PsiElementView<PsiModifierListOwner, PsiElementView> {

    private final Kind kind;

    private PsiGuardTarget(@NotNull PsiModifierListOwner element, @NotNull Kind kind) {
        super(element, null);
        this.kind = kind;
    }

    @NullIf("Element not guardable")
    public static PsiGuardTarget get(@Nullable PsiModifierListOwner element) {
        if ( element == null ) {
            return null;
        }
        Kind kind = Kind.of(element);
        if ( kind == null ) {
            return null;
        }
        return new PsiGuardTarget(element, kind);
    }

    @Contract("null -> false")
    public static boolean isGuardable(@Nullable PsiElement element) {
        return Kind.of(element) != null;
    }

    @NotNull
    public Kind getKind() {
        return kind;
    }

    @NotNull
    public PsiType getType() {
        return kind.getType(element);
    }

    public boolean isGuardable() {
        //noinspection ConstantConditions
        return getType() != null
                && !getType().equals(PsiType.VOID)
                && !getType().equals(PsiType.NULL);
    }

    public FluentIterable<PsiGuardTarget> getParameters() {
        if ( kind != Kind.METHOD ) {
            return NullSafe.fluentIterable();
        }
        else {
            return fluentIterable((((PsiMethod)getElement()).getParameterList().getParameters()))
                    .transform(new Function<PsiParameter, PsiGuardTarget>() {
                        @Override
                        public PsiGuardTarget apply(@Nullable PsiParameter psiParameter) {
                            return get(psiParameter);
                        }
                    });
        }
    }

    public boolean hasGuards() {
        return getGuards().iterator().hasNext();
    }

    @NotNull
    public FluentIterable<PsiGuard> getGuards() {
        if ( element.getModifierList() == null ) {
            return fluentIterable();
        }
        return fluentIterable(element.getModifierList().getAnnotations())
                .transform(new Function<PsiAnnotation, PsiGuard>() {
                    @Override
                    public PsiGuard apply(@Nullable PsiAnnotation psiAnnotation) {
                        if ( psiAnnotation == null ) {
                            return null;
                        }
                        PsiGuardType guardType = PsiGuardType.ofPsiAnnotation(psiAnnotation);
                        if ( guardType != null ) {
                            return new PsiGuard(psiAnnotation, guardType, PsiGuardTarget.this);
                        }
                        return null;
                    }
                });
    }

    public enum Kind {
        METHOD(PsiMethod.class) {
            //@Override
            //public boolean isApplicable(@Nullable PsiElement element) {
            //    return super.isApplicable(element) && !((PsiMethod)element).isConstructor();
            //}

            @Override
            PsiType getType(PsiElement element) {
                return ((PsiMethod)element).getReturnType();
            }
        },
        PARAMETER(PsiParameter.class) {
            @Override
            PsiType getType(PsiElement element) {
                return ((PsiParameter)element).getType();
            }
        },
        FIELD(PsiField.class) {
            @Override
            PsiType getType(PsiElement element) {
                return ((PsiField)element).getType();
            }
        };


        private static final Kind[] values = values();
        private final Class<? extends PsiModifierListOwner> representationClass;

        Kind(Class<? extends PsiModifierListOwner> representationClass) {
            this.representationClass = representationClass;
        }

        public boolean isApplicable(@Nullable PsiElement element) {
            return representationClass.isInstance(element);
        }

        @NotNull
        public Class<? extends PsiModifierListOwner> representationClass() {
            return representationClass;
        }

        @Nullable
        public static Kind of(@Nullable PsiElement element) {
            if ( element == null ) {
                return null;
            }
            for( Kind k : values ) {
                if ( k.isApplicable(element) ) {
                    return k;
                }
            }
            return null;
        }

        abstract PsiType getType(PsiElement element);

    }

}
