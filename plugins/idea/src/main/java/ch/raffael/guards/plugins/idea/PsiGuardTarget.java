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

import java.util.LinkedList;

import com.google.common.base.Objects;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.util.PsiUtil;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
public class PsiGuardTarget {

    private final PsiElement sourceElement;
    private PsiElement element;
    private LinkedList<PsiElement> track = new LinkedList<>();

    private PsiElement visual = null;
    private PsiMethod target;
    private Integer targetIndex;

    public PsiGuardTarget(@Nullable PsiElement start) {
        sourceElement = element = start;
        findGuarded();
    }

    @Nullable
    private <T extends PsiElement> T push(@NotNull T element) {
        assert track.peek() != element;
        track.push(element);
        return element;
    }

    @Nullable
    private PsiElement peek() {
        return peek(0);
    }

    @Nullable
    private PsiElement peek(int i) {
        if ( i >= track.size() ) {
            return null;
        }
        else {
            return track.get(i);
        }
    }

    private void findGuarded() {
        while ( element != null && !(element instanceof PsiFile) ) {
            //appendInfo(this);
            if ( element instanceof PsiExpressionList && element.getParent() instanceof PsiMethodCallExpression ) {
                if ( peek() != null ) {
                    int argIndex = 0;
                    boolean hadExpression = false;
                    for( PsiElement child : element.getChildren() ) {
                        if ( child instanceof PsiExpression ) {
                            hadExpression = true;
                        }
                        if ( child == peek() ) {
                            if ( hadExpression ) {
                                PsiExpression[] args = ((PsiExpressionList)element).getExpressions();
                                if ( argIndex < args.length ) {
                                    visual = args[argIndex];
                                    targetIndex = argIndex;
                                }
                            }
                            else {
                                targetIndex = -1;
                            }
                            break;
                        }
                        else if ( PsiUtil.isJavaToken(child, JavaTokenType.COMMA) ) {
                            argIndex++;
                        }
                    }
                }
            }
            else if ( element instanceof PsiMethodCallExpression ) {
                updateVisual(((PsiMethodCallExpression)element).getMethodExpression().getLastChild());
                updateTarget();
                return;
            }
            else if ( element instanceof PsiParameterList && element.getParent() instanceof PsiMethod ) {
                if ( peek() != null ) {
                    int argIndex = 0;
                    boolean hadParameter = false;
                    for( PsiElement child : element.getChildren() ) {
                        if ( child instanceof PsiParameter ) {
                            hadParameter = true;
                        }
                        if ( child == peek() ) {
                            if ( hadParameter ) {
                                PsiParameter[] args = ((PsiParameterList)element).getParameters();
                                if ( argIndex < args.length ) {
                                    visual = args[argIndex].getNameIdentifier();
                                    targetIndex = argIndex;
                                }
                            }
                            else {
                                targetIndex = -1;
                            }
                            break;
                        }
                        else if ( PsiUtil.isJavaToken(child, JavaTokenType.COMMA) ) {
                            argIndex++;
                        }
                    }
                }
            }
            else if ( element instanceof PsiMethod ) {
                updateVisual(((PsiMethod)element).getNameIdentifier());
                updateTarget();
                return;
            }
            else if ( element instanceof PsiReturnStatement ) {
                //if ( element.get)
                updateVisual(element);
                targetIndex = -1;
            }
            push(element);
            element = element.getParent();
        }
    }

    private void updateVisual(PsiElement visual) {
        if ( this.visual == null ) {
            this.visual = visual;
        }
    }

    private void updateTarget() {
        if ( targetIndex == null ) {
            targetIndex = -1;
        }
        if ( element instanceof PsiMethodCallExpression ) {
            target = ((PsiMethodCallExpression)element).resolveMethod();
        }
        else if ( element instanceof PsiMethod ) {
            target = (PsiMethod)element;
        }
        //appendInfo(this);
    }

    public PsiElement getSourceElement() {
        return sourceElement;
    }

    public PsiElement getVisual() {
        return visual;
    }

    public PsiMethod getTarget() {
        return target;
    }

    public Integer getTargetIndex() {
        return targetIndex;
    }

    public PsiElement resolveTarget() {
        if ( target == null ) {
            return null;
        }
        assert targetIndex != null && targetIndex >= -1;
        if ( targetIndex == -1 ) {
            return target;
        }
        else {
            PsiParameter[] params = target.getParameterList().getParameters();
            if ( targetIndex < params.length ) {
                return params[targetIndex];
            }
            else if ( params.length > 0 && params[params.length - 1].isVarArgs() ) {
                return params[params.length - 1];
            }
            else {
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("target", target)
                .add("targetIndex", targetIndex)
                .add("element", element)
                .add("visual", visual)
                .toString();
    }
}
