package at.wtioit.intellij.plugins.odoo.models;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class OdooModelUtil {

    private static final Logger logger = Logger.getInstance(OdooModelUtil.class);
    public static final String NAME_WILDCARD_MARKER = ":ANYTHING:";
    private static final Map<String, Pattern> regexCache = new ConcurrentHashMap<>();

    public static String detectName(PsiElement pyline) {
        return detectName(pyline, () -> PyResolveContext.defaultContext().withTypeEvalContext(TypeEvalContext.codeAnalysis(pyline.getContainingFile().getProject(), pyline.getContainingFile())));
    }

    public static String detectName(PsiElement pyline, Supplier<PyResolveContext> contextSupplier) {
        String name = null;
        for (PsiElement statement : pyline.getChildren()[1].getChildren()) {
            PsiElement statementFirstChild = statement.getFirstChild();
            if (statementFirstChild != null) {
                String variableName = statementFirstChild.getText();
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
        } else if (valueChild instanceof PyBinaryExpression) {
            PyElementType operator = ((PyBinaryExpression) valueChild).getOperator();
            if (operator != null) {
                if ("__mod__".equals(operator.getSpecialMethodName())) {
                    String formattedName = getStringValueForValueChild(valueChild.getFirstChild(), contextSupplier);
                    if (formattedName != null) {
                        return formattedName.replaceAll("%s", NAME_WILDCARD_MARKER);
                    }
                }
            }
        } else if (valueChild instanceof PyCallExpression || valueChild instanceof PySubscriptionExpression) {
            logger.debug("Cannot detect string value for class: " + valueChild.getClass());
        } else if (valueChild instanceof PsiErrorElement) {
            // ignore PsiErrorElements (used to indicate errors in Editor)
        } else {
            logger.error("Unknown string value class: " + valueChild.getClass());
        }
        return null;
    }

    public static boolean wildcardNameMatches(String nameWithWildcard, String name) {
        if (!nameWithWildcard.contains(NAME_WILDCARD_MARKER))  {
            return false;
        }
        if (!regexCache.containsKey(nameWithWildcard)) {
            String rePattern = nameWithWildcard.replaceAll("\\.", "\\\\.").replace(NAME_WILDCARD_MARKER, ".*");
            regexCache.put(nameWithWildcard, Pattern.compile(rePattern));
        }
        Pattern pattern = regexCache.get(nameWithWildcard);
        return pattern.matcher(name).matches();
    }

    public static String removeWildcards(String modelName) {
        return modelName.replaceAll(NAME_WILDCARD_MARKER, "");
    }
}
