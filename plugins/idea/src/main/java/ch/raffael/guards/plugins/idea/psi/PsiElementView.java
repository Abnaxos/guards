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


import com.google.common.base.Objects;
import com.intellij.psi.PsiElement;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class PsiElementView<T extends PsiElement, P extends PsiElementView> {

    protected final T element;
    protected final P parent;

    protected PsiElementView(@NotNull T element, @Nullable P parent) {
        this.element = element;
        this.parent = parent;
    }

    @NotNull
    public T getElement() {
        return element;
    }

    @NotNull
    public P getParent() {
        if ( parent == null ) {
            throw new IllegalStateException(this + " has no parent");
        }
        return parent;
    }

    public boolean hasParent() {
        return parent != null;
    }

    @Nullable
    public P getParentOrNull() {
        return parent;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(element, parent);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null || getClass() != obj.getClass() ) {
            return false;
        }
        final PsiElementView other = (PsiElementView)obj;
        return Objects.equal(this.element, other.element)
                && Objects.equal(this.parent, other.parent);
    }

    @Override
    public String toString() {
        Objects.ToStringHelper toString = Objects.toStringHelper(this).addValue(element);
        if ( parent != null ) {
            toString.addValue(parent);
        }
        return toString.toString();
    }
}
