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
import javax.swing.ImageIcon;

import com.intellij.ide.IconLayerProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import ch.raffael.guards.NotNull;

import static ch.raffael.guards.plugins.idea.PsiGuardUtil.isGuarded;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class GuardIcons implements IconLayerProvider {

    private static Icon load(String resPath) {
        return new ImageIcon(GuardIcons.class.getResource(resPath));
    }

    public static final Icon Guard = load("guards.png");
    public static final Icon GuardLayer = load("guard-layer.png");

    @Nullable
    @Override
    public Icon getLayerIcon(@NotNull Iconable element, boolean isLocked) {
        if ( element instanceof PsiElement && isGuarded((PsiElement)element) ) {
            return GuardLayer;
        }
        else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getLayerDescription() {
        return "Layer for guard annotations";
    }
}
