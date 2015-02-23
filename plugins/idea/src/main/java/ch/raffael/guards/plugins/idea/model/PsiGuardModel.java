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

package ch.raffael.guards.plugins.idea.model;

import com.google.common.base.Preconditions;
import com.intellij.psi.PsiClass;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.analysis.Context;
import ch.raffael.guards.analysis.GuardModel;
import ch.raffael.guards.analysis.LoadException;
import ch.raffael.guards.analysis.NameStyle;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class PsiGuardModel extends GuardModel {

    private final PsiClass psiClass;

    protected PsiGuardModel(@NotNull Context<PsiGuardModel> context, @NotNull PsiClass psiClass) {
        super(context, Preconditions.checkNotNull(psiClass.getQualifiedName(), "Anonymous guard type: %s", psiClass));
        this.psiClass = psiClass;
    }

    @NotNull
    @Override
    public String getName(NameStyle style) {
        if ( style == NameStyle.SHORT ) {
            StringBuilder buf = new StringBuilder(psiClass.getName());
            PsiClass outer = psiClass;
            while ( (outer = psiClass.getContainingClass()) != null ) {
                buf.insert(0, '.');
                buf.insert(0, outer.getName());
            }
            return buf.toString();
        }
        return super.getName(style);
    }

    @NotNull
    public PsiClass getPsiClass() {
        return psiClass;
    }

    @Override
    protected void load() throws LoadException {
        // FIXME: Not implemented
    }
}
