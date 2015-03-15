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

import com.intellij.ide.IconProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import ch.raffael.guards.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsIconProvider extends IconProvider {

    @Nullable
    @Override
    public Icon getIcon(@NotNull PsiElement element, int flags) {
        if ( PsiGuardUtil.isGuardAnnotation(element) ) {
            return GuardIcons.Guard;
        }
        else {
            return null;
        }
    }
}
