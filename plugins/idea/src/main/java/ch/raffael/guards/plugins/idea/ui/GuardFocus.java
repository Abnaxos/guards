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

import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;

import ch.raffael.guards.Immutable;
import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.Unsigned;
import ch.raffael.guards.UnsignedOrSpecial;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Immutable
public final class GuardFocus {

    public static final DataKey<GuardFocus> GUARD_FOCUS_KEY = DataKey.<GuardFocus>create(GuardFocus.class.getName());

    private final PsiElement origin;

    private Set<PsiElement> methodVisuals;
    private List<Set<PsiElement>> parameterVisuals;

    private PsiMethod method;
    private List<PsiParameter> parameters;

    private int index;

    private GuardFocus(PsiElement origin) {
        this.origin = origin;
        if ( findMethodCall(origin) ) {
            return;
        }
        if ( findConstructorCall(origin) ) {
            return;
        }
        if ( findMethod(origin) ) {
            //noinspection UnnecessaryReturnStatement
            return;
        }
        if ( method != null ) {
            index = findOriginIndex();
        }
    }

    private GuardFocus(PsiMethod method) {
        this.origin = method;
        findMethod(method);
        index = -1;
    }

    private GuardFocus(@NotNull GuardFocus that, @UnsignedOrSpecial int index) {
        origin = that.origin;
        methodVisuals = that.methodVisuals;
        parameterVisuals = that.parameterVisuals;
        method = that.method;
        parameters = that.parameters;
        this.index = index;
    }

    @NotNull
    public static GuardFocus create(@NotNull PsiMethod method) {
        return new GuardFocus(method);
    }

    @Nullable
    public static GuardFocus find(@Nullable PsiElement start) {
        if ( start == null ) {
            return null;
        }
        GuardFocus focus = new GuardFocus(start);
        if ( focus.method == null ) {
            return null;
        }
        else {
            return focus;
        }
    }

    private boolean findMethodCall(PsiElement start) {
        PsiMethodCallExpression methodCall = PsiTreeUtil.getParentOfType(start, PsiMethodCallExpression.class, false);
        if ( methodCall == null ) {
            return false;
        }
        method = methodCall.resolveMethod();
        if ( method == null ) {
            return true;
        }
        parameters = ImmutableList.copyOf(method.getParameterList().getParameters());
        methodVisuals = ImmutableSet.copyOf(visuals(methodCall.getMethodExpression().getReferenceNameElement(), methodCall.getArgumentList()));
        ImmutableList.Builder<Set<PsiElement>> paramVisualsBuilder = ImmutableList.builder();
        PsiExpression[] paramExpressions = methodCall.getArgumentList().getExpressions();
        for( int i = 0; i < paramExpressions.length; i++ ) {
            paramVisualsBuilder.add(ImmutableSet.<PsiElement>of(paramExpressions[i]));
        }
        parameterVisuals = paramVisualsBuilder.build();
        return true;
    }

    private boolean findConstructorCall(PsiElement start) {
        PsiNewExpression constructorCall = PsiTreeUtil.getParentOfType(start, PsiNewExpression.class, false);
        if ( constructorCall == null ) {
            return false;
        }
        method = constructorCall.resolveMethod();
        if ( method == null ) {
            return true;
        }
        parameters = ImmutableList.copyOf(method.getParameterList().getParameters());
        if ( constructorCall.getClassOrAnonymousClassReference() != null ) {
            methodVisuals = ImmutableSet.of(constructorCall.getClassOrAnonymousClassReference().getReferenceNameElement(), method.getTypeParameterList());
        }
        else {
            methodVisuals = null;
        }
        if ( constructorCall.getArgumentList() != null ) {
            PsiExpression[] paramExpressions = constructorCall.getArgumentList().getExpressions();
            ImmutableList.Builder<Set<PsiElement>> paramVisualsBuilder = ImmutableList.builder();
            for( int i = 0; i < paramExpressions.length; i++ ) {
                paramVisualsBuilder.add(ImmutableSet.<PsiElement>of(paramExpressions[i]));
            }
            parameterVisuals = paramVisualsBuilder.build();
        }
        else {
            parameterVisuals = null;
        }
        return true;
    }

    private boolean findMethod(PsiElement start) {
        method = PsiTreeUtil.getParentOfType(start, PsiMethod.class, false);
        if ( method == null ) {
            return false;
        }
        parameters = ImmutableList.copyOf(method.getParameterList().getParameters());
        methodVisuals = ImmutableSet.of(method.getNameIdentifier(), method.getParameterList());
        ImmutableList.Builder<Set<PsiElement>> paramVisualsBuilder = ImmutableList.builder();
        parameterVisuals = ImmutableList.copyOf(Lists.transform(parameters, new Function<PsiParameter, Set<PsiElement>>() {
            @Override
            public Set<PsiElement> apply(PsiParameter psiParameter) {
                return ImmutableSet.<PsiElement>of(psiParameter);
            }
        }));
        return true;
    }

    @NotNull
    public PsiMethod getMethod() {
        return method;
    }

    @NotNull
    public List<PsiParameter> getParameters() {
        return parameters;
    }

    @NotNull
    public PsiParameter getParameter(@Unsigned int index) {
        return parameters.get(parameterIndexVarArgs(index));
    }

    @UnsignedOrSpecial
    public int getIndex() {
        return index;
    }

    @NotNull
    public PsiModifierListOwner getElement() {
        return getElement(index);
    }

    @NotNull
    public PsiModifierListOwner getElement(@UnsignedOrSpecial int index) {
        if ( index < 0 ) {
            return getMethod();
        }
        else {
            return getParameter(index);
        }
    }

    @NotNull
    public PsiElement getOrigin() {
        return origin;
    }

    @Nullable
    public TextRange visualRange(@NotNull Editor editor, @NotNull Project project) {
        if ( index < 0 ) {
            return methodVisualRange(editor, project);
        }
        else {
            return parameterVisualRange(editor, project, index);
        }
    }

    @Nullable
    public TextRange methodVisualRange(Editor editor, Project project) {
        return textRange(editor, project, methodVisuals);
    }

    @Nullable
    public Set<PsiElement> methodVisuals() {
        return methodVisuals;
    }

    @Nullable
    public TextRange parameterVisualRange(@NotNull Editor editor, @NotNull Project project, int parameterIndex) {
        if ( parameterVisuals == null ) {
            return null;
        }
        return textRange(editor, project, parameterVisuals.get(parameterIndexVarArgs(parameterIndex)));
    }

    @Nullable
    public Set<PsiElement> parameterVisuals(int parameterIndex) {
        return parameterVisuals.get(parameterIndexVarArgs(parameterIndex));
    }

    @NotNull
    public GuardFocus forIndex(@UnsignedOrSpecial int index) {
        if ( index != this.index ) {
            return new GuardFocus(this, index);
        }
        else {
            return this;
        }
    }

    @Nullable
    public TextRange findTextRange(@NotNull final Editor editor, @NotNull final Project project, @Nullable PsiElement element) {
        if ( element == null ) {
            return null;
        }
        if ( methodVisuals == null || parameterVisuals == null ) {
            return null;
        }
        while ( element != null ) {
            if ( methodVisuals.contains(element) ) {
                return textRange(editor, project, methodVisuals);
            }
            if ( parameterVisuals != null ) {
                int index = 0;
                for( Set<PsiElement> param : parameterVisuals ) {
                    if ( param.contains(element) ) {
                        return textRange(editor, project, param);
                    }
                }
            }
            element = element.getParent();
        }
        return null;
    }

    @Nullable
    public Integer findParameterIndex(@Nullable PsiElement element) {
        if ( element == null ) {
            return null;
        }
        if ( methodVisuals == null || parameterVisuals == null ) {
            return null;
        }
        while ( element != null ) {
            if ( methodVisuals.contains(element) ) {
                return -1;
            }
            if ( parameterVisuals != null ) {
                int index = 0;
                for( Set<PsiElement> param : parameterVisuals ) {
                    if ( param.contains(element) ) {
                        return index;
                    }
                }
            }
            element = element.getParent();
        }
        return null;
    }

    public int findOriginIndex() {
        Integer index = findParameterIndex(origin);
        if ( index == null ) {
            throw new IllegalStateException("No parameter index found for origin " + origin);
        }
        return index;
    }

    private int parameterIndexVarArgs(@Unsigned int paramIndex) {
        if ( paramIndex > parameters.size() ) {
            if ( parameters.size() > 0 && parameters.get(parameters.size() - 1 - 1).isVarArgs() ) {
                return parameters.size() - 1;
            }
            throw new IndexOutOfBoundsException(paramIndex + ">=" + parameters.size());
        }
        else {
            return paramIndex;
        }
    }

    @Nullable
    private static TextRange textRange(@NotNull Editor editor, @NotNull Project project, @Nullable Iterable<? extends PsiElement> visuals) {
        if ( visuals == null ) {
            return null;
        }
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        if ( psiFile == null ) {
            return null;
        }
        TextRange range = null;
        for( PsiElement visual : visuals ) {
            if ( !psiFile.equals(visual.getContainingFile()) ) {
                return null;
            }
            if ( range == null ) {
                range = visual.getTextRange();
            }
            else if ( visual.getTextRange() != null ) {
                range = range.union(visual.getTextRange());
            }
        }
        return range;
    }


    @NotNull
    private static PsiElement[] visuals(@NotNull PsiElement... elements) {
        return elements;
    }



}
