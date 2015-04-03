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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterators;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.plugins.idea.util.InspectionQueue;

import static ch.raffael.guards.plugins.idea.util.NullSafe.fluentIterable;
import static java.util.Arrays.asList;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class PsiHandlerClass extends PsiElementView<PsiClass, PsiGuardType> {

    PsiHandlerClass(@NotNull PsiClass psiClass, @NotNull PsiGuardType psiGuardType) {
        super(psiClass, psiGuardType);
    }

    @NotNull
    public PsiClass getPsiClass() {
        return element;
    }

    @NotNull
    public PsiGuardType getPsiGuardType() {
        return getParent();
    }

    @NotNull
    public FluentIterable<PsiTestMethod> getTestMethods() {
        return fluentIterable(new Iterable<PsiMethod>() {
            @Override
            public Iterator<PsiMethod> iterator() {
                return new AbstractIterator<PsiMethod>() {
                    private final InspectionQueue<PsiClass> classes = new InspectionQueue<>(element);
                    private final Set<PsiMethod> seenMethods = new HashSet<PsiMethod>();
                    private Iterator<PsiMethod> methods = null;

                    @Override
                    protected PsiMethod computeNext() {
                        while ( methods == null || !methods.hasNext() ) {
                            PsiClass c = classes.poll();
                            if ( c == null ) {
                                return endOfData();
                            }
                            classes.addAll(asList(c.getInterfaces()));
                            classes.add(c.getSuperClass());
                            methods = Iterators.filter(asList(c.getAllMethods()).iterator(),
                                    new Predicate<PsiMethod>() {
                                        @Override
                                        public boolean apply(@Nullable PsiMethod method) {
                                            return PsiTestMethod.isTestMethod(method, seenMethods);
                                        }
                                    });
                        }
                        return methods.next();
                    }
                };
            }
        }).transform(new Function<PsiMethod, PsiTestMethod>() {
            @Override
            public PsiTestMethod apply(@Nullable PsiMethod psiMethod) {
                return psiMethod == null ? null : new PsiTestMethod(psiMethod, PsiHandlerClass.this);
            }
        });
    }

}
