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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.google.common.base.Objects;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsView {

    private JPanel root;
    private JTextArea elementInfo;

    public JComponent getComponent() {
        return root;
    }

    public PsiGuardTarget updateElement(PsiElement currentElement) {
        elementInfo.setText("");
        PsiGuardTarget target = new PsiGuardTarget(currentElement);
        return updateElement(target);
    }

    public PsiGuardTarget updateElement(PsiGuardTarget target) {
        elementInfo.setText("SourceElement: " + target.getSourceElement()
                + "\nVisual: " + target.getVisual()
                + "\nTarget: " + target.getTarget() + " / " + target.getTargetIndex()
                + "\nResolved Target: " + target.resolveTarget());
        return target;
    }

    private void appendInfo(Object info) {
        if ( elementInfo.getText() != null && !elementInfo.getText().isEmpty() ) {
            elementInfo.append("\n");
        }
        elementInfo.append(String.valueOf(info));
    }

    //private PsiElement findGuardedElement(GuardSelection current, GuardSelection previous) {
    //    elementInfo.setText(elementInfo.getText() + "\n" + String.valueOf(current));
    //    if ( current == null || current instanceof PsiFile ) {
    //        return null;
    //    }
    //    else if ( current instanceof PsiMethod || current instanceof PsiParameter ) {
    //        //elementInfo.setText(elementInfo.getText() + " ***");
    //        return current;
    //    }
    //    else if ( current instanceof PsiExpressionList ) {
    //        return findGuardedElement(current.getParent(), previous);
    //    }
    //    else if ( current instanceof PsiMethodReferenceExpression ) {
    //        return findGuardedElement(((PsiMethodReferenceExpression)current).getPotentiallyApplicableMember(), current);
    //    }
    //    else if ( current instanceof PsiMethodCallExpression ) {
    //        PsiMethod method = ((PsiMethodCallExpression)current).resolveMethod();
    //        if ( method != null ) {
    //            if ( previous != null ) {
    //                PsiExpression[] args = ((PsiMethodCallExpression)current).getArgumentList().getExpressions();
    //                for( int i = 0; i < args.length; i++ ) {
    //                    if ( args[i].equals(previous) ) {
    //                        PsiParameter[] parameters = method.getParameterList().getParameters();
    //                        if ( i < parameters.length ) {
    //                            return parameters[i];
    //                        }
    //                    }
    //                }
    //            }
    //            return method;
    //        }
    //    }
    //    //else {
    //        return findGuardedElement(current.getParent(), current);
    //    //}
    //}

    public static class GuardSelection {

        private final PsiElement highlightElement;
        private final PsiMember guardedElement;

        public GuardSelection(PsiElement highlightElement, PsiMember guardedElement) {
            this.highlightElement = highlightElement;
            this.guardedElement = guardedElement;
        }

        public PsiElement getHighlightElement() {
            return highlightElement;
        }

        public PsiMember getGuardedElement() {
            return guardedElement;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("highlightElement", highlightElement)
                    .add("guardedElement", guardedElement)
                    .toString();
        }
    }



}
