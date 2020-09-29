package at.wtioit.intellij.plugins.odoo.models;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.python.psi.PyBinaryExpression;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyListLiteralExpression;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class OdooModelUtil {

    private static final Logger logger = Logger.getInstance(OdooModelUtil.class);

    public static String detectName(PsiElement pyline) {
        return detectName(pyline, () -> PyResolveContext.defaultContext().withTypeEvalContext(TypeEvalContext.codeAnalysis(pyline.getContainingFile().getProject(), pyline.getContainingFile())));
    }

    public static String detectName(PsiElement pyline, Supplier<PyResolveContext> contextSupplier) {
        String name = null;
        for (PsiElement statement : pyline.getChildren()[1].getChildren()) {
            String variableName = statement.getFirstChild().getText();
            if (OdooModel.ODOO_MODEL_NAME_VARIABLE_NAME.contains(variableName)) {
                PsiElement valueChild = statement.getLastChild();
                while (valueChild instanceof PsiComment || valueChild instanceof PsiWhiteSpace) {
                    valueChild = valueChild.getPrevSibling();
                }
                String stringValueForChild = getStringValueForValueChild(valueChild, contextSupplier);
                if (stringValueForChild != null) {
                    name = stringValueForChild;
                }
                if ("_name".equals(variableName)) break;
            }
        }
        if (name == null) logger.debug("Cannot detect name for " + pyline + " in " + pyline.getContainingFile());
        return name;
    }

    private static String getStringValueForValueChild(@NotNull PsiElement valueChild, Supplier<PyResolveContext> contextSupplier) {
        if (valueChild instanceof PyStringLiteralExpressionImpl) {
            return ((PyStringLiteralExpressionImpl) valueChild).getStringValue();
        } else if (valueChild instanceof PyListLiteralExpression) {
            //firstChild() somehow returns the bracket
            PsiElement firstChild = valueChild.getChildren()[0];
            if (firstChild instanceof PyStringLiteralExpressionImpl) {
                return ((PyStringLiteralExpressionImpl) firstChild).getStringValue();
            } else {
                logger.error("Unknown string value class: " + valueChild.getClass());
            }
        } else if (valueChild instanceof PyReferenceExpression) {
            PyReferenceExpression expression = (PyReferenceExpression) valueChild;
            PsiElement resolvedElement = expression.followAssignmentsChain(contextSupplier.get()).getElement();
            if (resolvedElement != null) {
                return getStringValueForValueChild(resolvedElement, contextSupplier);
            } else {
                logger.debug("Cannot detect string value for " + valueChild.getReference());
            }
        } else if (valueChild instanceof PyCallExpression || valueChild instanceof PyBinaryExpression) {
            logger.debug("Cannot detect string value for class: " + valueChild.getClass());
        } else {
            logger.error("Unknown string value class: " + valueChild.getClass());
        }
        return null;
    }
}
