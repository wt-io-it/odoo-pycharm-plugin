package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyKeyValueExpression;
import com.jetbrains.python.psi.PyStringElement;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.TypeEvalContext;

public interface OdooModulePsiElementMatcherUtil {
    static boolean isOdooModulePsiElement(PsiElement psiElement) {
        if (psiElement instanceof PyStringElement) {
            PyKeyValueExpression keyValueExpression = PsiElementsUtil.findParent(psiElement, PyKeyValueExpression.class, 3);
            if (keyValueExpression != null) {
                String value = PsiElementsUtil.getStringValueForValueChild(keyValueExpression.getKey(), () -> PyResolveContext.defaultContext().withTypeEvalContext(TypeEvalContext.codeAnalysis(psiElement.getContainingFile().getProject(), psiElement.getContainingFile())));
                return "depends".equals(value);
            }
        }
        return false;
    }
}
