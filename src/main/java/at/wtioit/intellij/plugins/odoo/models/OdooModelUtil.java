package at.wtioit.intellij.plugins.odoo.models;

import at.wtioit.intellij.plugins.odoo.index.IndexWatcher;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
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
                    // if we get the name from _name it takes precedence over _inherit
                    if ("_name".equals(variableName)) break;
                }
            }
        }
        if (name == null) logger.debug("Cannot detect name for " + pyline + " in " + pyline.getContainingFile());
        return name;
    }

    public static String getStringValueForValueChild(@NotNull PsiElement valueChild) {
        return getStringValueForValueChild(valueChild, () -> PyResolveContext.defaultContext().withTypeEvalContext(TypeEvalContext.codeAnalysis(valueChild.getContainingFile().getProject(), valueChild.getContainingFile())));
    }

    public static String getStringValueForValueChild(@NotNull PsiElement valueChild, Supplier<PyResolveContext> contextSupplier) {
        if (valueChild instanceof PyNoneLiteralExpression) {
            // _name = None, see src/test/resources/odoo/addons/module_indexing_special_cases/models/abstract_model.py
            return null;
        } else if (valueChild instanceof StringLiteralExpression) {
            return ((StringLiteralExpression) valueChild).getStringValue();
        } else if (valueChild instanceof PyStringElement) {
            TextRange contentRange = ((PyStringElement) valueChild).getContentRange();
            return valueChild.getText().substring(contentRange.getStartOffset(), contentRange.getEndOffset());
        } else if (valueChild instanceof PyListLiteralExpression || valueChild instanceof PySetLiteralExpression || valueChild instanceof PyTupleExpression) {
            //firstChild() somehow returns the bracket
            PsiElement firstChild = valueChild.getChildren()[0];
            return getStringValueForValueChild(firstChild);
        } else if (valueChild instanceof PyReferenceExpression) {
            if (!IndexWatcher.isCalledInIndexJob()) {
                // this is only safe when not running in index job as the followAssignmentsChain may load other
                // python files and this leads to `Indexing process should not rely on non-indexed file data`
                PyReferenceExpression expression = (PyReferenceExpression) valueChild;
                PsiElement resolvedElement = expression.followAssignmentsChain(contextSupplier.get()).getElement();
                if (resolvedElement != null) {
                    return getStringValueForValueChild(resolvedElement, contextSupplier);
                } else {
                    logger.debug("Cannot detect string value for " + valueChild.getReference());
                }
            }
            // TODO when running in index job add some magic that lets us look up self.SOMETHING within the same file
            // like we do for superclasses at/wtioit/intellij/plugins/odoo/OdooModelPsiElementMatcherUtil.java:201
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
        } else if (valueChild instanceof PyParenthesizedExpression) {
            // for values in parentheses we return the value for the contained expression
            // which should be correct at least for non lists
            // https://docs.python.org/3/reference/expressions.html#parenthesized-forms
            PyExpression containedExpression = ((PyParenthesizedExpression) valueChild).getContainedExpression();
            if (containedExpression != null) {
                return getStringValueForValueChild(containedExpression, contextSupplier);
            }
        } else if (valueChild instanceof PyDictLiteralExpression) {
            // firstChild() somehow returns the bracket
            PsiElement firstChild = valueChild.getChildren()[0];
            if (firstChild instanceof PyKeyValueExpression) {
                return getStringValueForValueChild(((PyKeyValueExpression) firstChild).getKey(), contextSupplier);
            }
        } else if (valueChild instanceof PyConditionalExpression) {
            // ignore conditional expressions for names (we cannot resolve them yet)
            //   _name = 'value' if condition else 'other_value'
            // see src/test/resources/odoo/addons/addon1/models/conditional.py
            // TODO try to resolve conditional expressions
            return null;
        } else if (valueChild instanceof PsiErrorElement) {
            // ignore PsiErrorElements (used to indicate errors in Editor)
            return null;
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
