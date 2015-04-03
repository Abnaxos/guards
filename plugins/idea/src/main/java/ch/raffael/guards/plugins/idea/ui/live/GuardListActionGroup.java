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

package ch.raffael.guards.plugins.idea.ui.live;

import com.google.common.base.Function;
import com.intellij.openapi.actionSystem.AnAction;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.plugins.idea.psi.PsiGuard;
import ch.raffael.guards.plugins.idea.psi.PsiGuardTarget;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ComponentNotRegistered")
public class GuardListActionGroup extends AbstractGuardPopupGroup<PsiGuardTarget> {

    public GuardListActionGroup(GuardPopupController controller, PsiGuardTarget guardable) {
        super(controller, guardable);
        init(guardable);
    }

    public GuardListActionGroup(GuardPopupAction<?> parent, PsiGuardTarget guardable) {
        super(parent, guardable);
        init(guardable);
    }

    protected void init(@NotNull PsiGuardTarget guardable) {
        setPopup(false);
        add(guardable.getGuards().transform(new Function<PsiGuard, AnAction>() {
            @Override
            public AnAction apply(PsiGuard guard) {
                return new GuardActionGroup(GuardListActionGroup.this, guard);
            }
        }));
    }

}
