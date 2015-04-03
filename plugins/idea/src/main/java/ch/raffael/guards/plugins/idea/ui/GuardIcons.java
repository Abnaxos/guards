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

package ch.raffael.guards.plugins.idea.ui;

import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IconLayerProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiClass;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IconUtil;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.plugins.idea.psi.PsiGuardType;

import static ch.raffael.guards.plugins.idea.util.NullSafe.cast;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class GuardIcons implements IconLayerProvider {

    @NotNull
    private static Icon load(@NotNull String resPath) {
        return new ImageIcon(GuardIcons.class.getResource(resPath));
    }

    public static final Icon Guard = load("guards.png");
    public static final Icon GutterGuard = load("guards12.png");
    public static final Icon GutterGuardWarning = new LayeredIcon(GutterGuard,
            IconUtil.cropIcon(AllIcons.General.WarningDecorator, new Rectangle(4, 0, 12, 12)));
    public static final Icon GuardLayer = load("guard-layer.png");

    public static final Icon EditGuardsAction = load("edit-guards.png");

    @Nullable
    @Override
    public Icon getLayerIcon(@NotNull Iconable element, boolean isLocked) {
        if ( PsiGuardType.isGuardType(cast(PsiClass.class, element)) ) {
            return GuardLayer;
        }
        else {
            return null;
        }
    }

    @Override
    public String getLayerDescription() {
        return "Layer for guard annotations";
    }
}
