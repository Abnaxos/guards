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

import java.util.ArrayList;
import java.util.List;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ch.raffael.guards.definition.Guard;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class GuardInfo {

    private static final GuardInfo NO_GUARD = new GuardInfo() {
        @Override
        public boolean isGuard() {
            return false;
        }
        @Override
        public String toString() {
            return "GuardInfo{not-a-guard}";
        }
    };

    GuardInfo() {
    }

    public abstract boolean isGuard();

    @NotNull
    public static Key<GuardInfo> key() {
        return new Key<>(GuardInfo.class.getName());
    }

    @NotNull
    public static GuardInfo noGuard() {
        return noGuard(null);
    }

    @NotNull
    public static GuardInfo noGuard(@Nullable UserDataHolder keep) {
        if ( keep != null ) {
            keep.putUserData(key(), NO_GUARD);
        }
        return NO_GUARD;
    }

    @NotNull
    public static List<GuardInfo> forPsiMethod(@NotNull PsiMethod method) {
        final List<GuardInfo> guardInfos = new ArrayList<>();
        for( PsiAnnotation annotation : method.getModifierList().getAnnotations() ) {
            GuardInfo guardInfo = GuardInfo.forPsiAnnotation(annotation);
            if ( guardInfo.isGuard() ) {
                guardInfos.add(guardInfo);
            }
        }
        return guardInfos;
    }

    public static GuardInfo forPsiAnnotation(@NotNull PsiAnnotation annotation) {
        GuardInfo guardInfo = annotation.getUserData(GuardInfo.key());
        if ( guardInfo != null ) {
            return guardInfo;
        }
        if ( annotation.getQualifiedName() == null ) {
            return noGuard(annotation);
        }
        PsiClass annotationClass = JavaPsiFacade.getInstance(annotation.getProject()).findClass(annotation.getQualifiedName(), annotation.getResolveScope());
        if ( annotationClass == null ) {
            return noGuard(annotation);
        }
        return store(annotation, forPsiClass(annotationClass));
    }

    public static GuardInfo forPsiClass(@NotNull PsiClass annotationClass) {
        GuardInfo guardInfo = annotationClass.getUserData(GuardInfo.key());
        if ( guardInfo != null ) {
            return guardInfo;
        }
        if ( annotationClass.getModifierList() == null ) {
            return noGuard(annotationClass);
        }
        for( PsiAnnotation metaAnnotation : annotationClass.getModifierList().getAnnotations() ) {
            if ( Guard.class.getName().equals(metaAnnotation.getQualifiedName()) ) {
                return store(annotationClass, new PsiGuardInfo(annotationClass));
            }
        }
        return noGuard(annotationClass);
    }

    @NotNull
    public static GuardInfo store(UserDataHolder holder, @NotNull  GuardInfo guardInfo) {
        // TODO: probably not the right place to cache information
        //holder.putUserData(key(), guardInfo);
        return guardInfo;
    }

}
