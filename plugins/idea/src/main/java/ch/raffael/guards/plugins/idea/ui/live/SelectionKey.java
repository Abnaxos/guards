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

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.ext.NullIf;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class SelectionKey<E extends PsiElement> {

    private final E element;
    private final Option option;

    public SelectionKey(@NotNull E element) {
        this(element, null);
    }

    public SelectionKey(@NotNull E element, @Nullable Option option) {
        this.element = element;
        this.option = option;
    }

    @NullIf("No selection")
    public static <E extends PsiElement> SelectionKey<E> of(@NullIf("No selection") E element) {
        return of(element, null);
    }

    @NullIf("No selection")
    public static <E extends PsiElement> SelectionKey<E> of(@NullIf("No selection")  E element, @Nullable Option option) {
        return element == null ? null : new SelectionKey<>(element, option);
    }

    public boolean isSelectableBy(@Nullable SelectionKey<?> selector) {
        if ( selector == null ) {
            return false;
        }
        if ( !PsiTreeUtil.isAncestor(element, selector.element, false) ) {
            return false;
        }
        return option == null || option.equals(selector.option);
    }

    @NotNull
    public SelectionKey<E> elementKey() {
        if ( option != null ) {
            return new SelectionKey<E>(element, null);
        }
        else {
            return this;
        }
    }

    @NotNull
    public E getElement() {
        return element;
    }

    @Nullable
    public Option getOption() {
        return option;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        SelectionKey that = (SelectionKey)o;
        return element.equals(that.element) && option == that.option;
    }

    @Override
    public int hashCode() {
        int result = element.hashCode();
        result = 31 * result + (option != null ? option.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(getClass().getSimpleName());
        buf.append('{').append(element);
        if ( option != null ) {
            buf.append(':').append(option);
        }
        return buf.append('}').toString();
    }

    public static enum Option {
        INSERT, EDIT, DELETE, PULL_UP, PUSH_DOWN
    }

}
