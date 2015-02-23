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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Processor;

import ch.raffael.guards.NoNulls;
import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.analysis.Context;
import ch.raffael.guards.analysis.Type;
import ch.raffael.guards.definition.Guard;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class PsiGuardContext extends Context<PsiGuardModel> {

    private final Module module;
    private final JavaPsiFacade javaPsiFacade;

    private volatile boolean didFindAll = false;

    public PsiGuardContext(Module module) {
        super();
        this.module = module;
        javaPsiFacade = JavaPsiFacade.getInstance(module.getProject());
    }

    @Nullable
    @Override
    protected Type findType(@NotNull String name) {
        // FIXME: Not implemented
        return null;
    }

    @Nullable
    @Override
    public PsiGuardModel findGuard(@NotNull String name) {
        PsiClass psiClass = javaPsiFacade.findClass(name, searchScope());
        if ( psiClass == null ) {
            return null;
        }
        if ( !psiClass.isAnnotationType() ) {
            return null;
        }
        if ( psiClass.getModifierList() == null ) {
            return null;
        }
        PsiClass guardAnnotation = guardAnnotation();
        if ( guardAnnotation == null ) {
            return null;
        }
        return findGuard(psiClass);
    }

    @Nullable
    protected PsiGuardModel findGuard(@NotNull PsiClass psiClass) {
        if ( !psiClass.isAnnotationType() ) {
            return null;
        }
        if ( psiClass.getModifierList() == null ) {
            return null;
        }
        PsiClass guardAnnotation = guardAnnotation();
        if ( guardAnnotation == null ) {
            return null;
        }
        assert guardAnnotation.getQualifiedName() != null;
        if ( AnnotationUtil.isAnnotated(psiClass, guardAnnotation.getQualifiedName(), false) ) {
            return new PsiGuardModel(this, psiClass);
        }
        else {
            return null;
        }
    }

    @NotNull @NoNulls
    public Collection<PsiGuardModel> findAllGuards() {
        if ( didFindAll ) {
            return getKnownGuards();
        }
        else {
            final PsiClass annotation = guardAnnotation();
            if ( annotation == null ) {
                return Collections.emptyList();
            }
            assert annotation.getQualifiedName() != null;
            final List<PsiGuardModel> result = new ArrayList<>();
            AnnotatedElementsSearch.searchElements(annotation, searchScope(), PsiClass.class).forEach(new Processor<PsiClass>() {
                @Override
                public boolean process(PsiClass psiClass) {
                    if ( psiClass.getQualifiedName() == null ) {
                        return true;
                    }
                    if ( !psiClass.isAnnotationType() ) {
                        return true;
                    }
                    if ( AnnotationUtil.isAnnotated(psiClass, annotation.getQualifiedName(), false) ) {
                        result.add(add(new PsiGuardModel(PsiGuardContext.this, psiClass)));
                    }
                    return true;
                }
            });
            didFindAll = true;
            return Collections.unmodifiableCollection(result);
        }
    }

    @Nullable
    private PsiClass guardAnnotation() {
        return javaPsiFacade.findClass(Guard.class.getName(), searchScope());
    }

    public GlobalSearchScope searchScope() {
        return module.getModuleWithDependenciesAndLibrariesScope(false);
    }

}
