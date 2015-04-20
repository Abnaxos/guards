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

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNameValuePair;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;

import static ch.raffael.guards.plugins.idea.psi.Psi.resolve;
import static ch.raffael.guards.plugins.idea.util.NullSafe.cast;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class PsiGuard extends PsiElementView<PsiAnnotation, PsiGuardTarget> {

    private final PsiGuardType guardType;

    PsiGuard(@NotNull PsiAnnotation annotation, @NotNull PsiGuardType guardType, @NotNull PsiGuardTarget parent) {
        super(annotation, parent);
        this.guardType = guardType;
    }

    @Nullable
    public static PsiGuard of(@Nullable PsiAnnotation annotation) {
        if ( annotation == null ) {
            return null;
        }
        PsiGuardType psiGuardType = PsiGuardType.ofPsiAnnotation(annotation);
        if ( psiGuardType == null ) {
            return null;
        }
        PsiModifierList modifierList = cast(PsiModifierList.class, annotation.getOwner());
        if ( modifierList == null ) {
            return null;
        }
        PsiGuardTarget target = PsiGuardTarget.get(cast(PsiModifierListOwner.class, modifierList.getParent()));
        if ( target == null ) {
            return null;
        }
        return new PsiGuard(annotation, psiGuardType, target);
    }

    @NotNull
    public PsiGuardType getGuardType() {
        return guardType;
    }

    @NotNull
    public String getDescription(boolean full) {
        if ( !element.equals(resolve(element.getNameReferenceElement())) ) {
            return element.getText();
        }
        StringBuilder buf = new StringBuilder();
        buf.append('@');
        buf.append(element.getQualifiedName());
        if ( full && element.getParameterList().getAttributes().length > 0 ) {
            buf.append('(');
            boolean first = true;
            for( PsiNameValuePair value : element.getParameterList().getAttributes() ) {
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

}
