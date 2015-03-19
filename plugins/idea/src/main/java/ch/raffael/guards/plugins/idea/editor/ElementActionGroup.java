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

package ch.raffael.guards.plugins.idea.editor;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.plugins.idea.Guardable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ComponentNotRegistered")
public class ElementActionGroup extends AbstractGuardPopupGroup<PsiModifierListOwner> {

    public ElementActionGroup(@NotNull GuardPopupController controller, @NotNull @Guardable PsiModifierListOwner element) {
        super(controller, element);
        init(element);
    }

    public ElementActionGroup(@NotNull GuardPopupAction parent, @NotNull @Guardable PsiModifierListOwner element) {
        super(parent, element);
        init(element);
    }

    protected void init(PsiModifierListOwner element) {
        PsiType type;
        if ( element instanceof PsiMethod ) {
            type = ((PsiMethod)element).getReturnType();
        }
        else if ( element instanceof PsiParameter ) {
            type = ((PsiParameter)element).getType();
        }
        else {
            throw new IllegalArgumentException(String.valueOf(element));
        }
        String typeString;
        if ( type == null ) {
            typeString = "";
        }
        else if ( type instanceof PsiClassType ) {
            typeString = ((PsiClassType)type).getClassName() + " ";
        }
        else {
            typeString = type.getCanonicalText() + " ";
        }
        caption(typeString  + ((PsiNamedElement)element).getName(), element.getIcon(0));
        add(new AddGuardActionGroup(this, element));
        add(new GuardListActionGroup(this, element));
    }

}
