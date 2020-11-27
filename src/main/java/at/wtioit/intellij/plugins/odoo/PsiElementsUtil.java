package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModelUtil;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyFromImportStatement;
import com.jetbrains.python.psi.PyStringElement;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.intellij.psi.PsiErrorElement;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PsiElementsUtil {

    Logger logger = Logger.getInstance(PsiElementsUtil.class);

    @Nullable
    static <T extends PsiElement> T findParent(@Nullable PsiElement element, Class<T> parentClass) {
        return findParent(element, parentClass, 100);
    }

    @Nullable
    static <T extends PsiElement> T findParent(@Nullable PsiElement element, Class<T> parentClass, Function<T, Boolean> parentMatch) {
        return findParent(element, parentClass, parentMatch, 100);
    }

    @Nullable
    static <T extends PsiElement> T findParent(@Nullable PsiElement element, Class<T> parentClass, int inspectionLimit) {
        return findParent(element, parentClass, (e) -> true, inspectionLimit);
    }

    @Nullable
    static <T extends PsiElement> T findParent(@Nullable PsiElement element, Class<T> parentClass, Function<T, Boolean> parentMatch, int inspectionLimit) {
        if (element == null) return null;
        PsiElement parent = element.getParent();
        for (int i = 0; parent != null && i < inspectionLimit; i++) {
            if (parentClass.isAssignableFrom(parent.getClass())) {
                T foundElement = parentClass.cast(parent);
                if (parentMatch.apply(foundElement)) {
                    return foundElement;
                }
            }
            parent = parent.getParent();
        }
        return null;
    }

    static <T extends NavigationItem> T findChildrenByClassAndName(PyFromImportStatement importStatement, Class<T> childClass, String name) {
        return Arrays.stream(importStatement.getChildren())
                .filter(c -> childClass.isAssignableFrom(c.getClass()))
                .map(childClass::cast)
                .filter(c -> Objects.equals(c.getName(), name))
                .findFirst()
                .orElse(null);
    }

    /**
     * walk the PsiElement tree (downwards)
     * @param element element to walk into
     * @param function function to investigate children, should return `true` if element is complete (does not need
     *                walking into) and return `false` if element is not complete (needs walking into)
     */
    static void walkTree(@Nullable PsiElement element, Function<PsiElement, Boolean> function) {
        walkTree(element, function, PsiElement.class);
    }

    /**
     * walk the PsiElement tree (downwards)
     * @param element element to walk into
     * @param function function to investigate children, should return `true` if element is complete (does not need
     *                walking into) and return `false` if element is not complete (needs walking into)
     */
    static <T extends PsiElement> void walkTree(@Nullable PsiElement element, Function<T, Boolean> function, Class<T> typeFilter) {
        walkTree(element, function, typeFilter, Integer.MAX_VALUE);
    }

    /**
     * walk the PsiElement tree (downwards)
     * @param element element to walk into
     * @param function function to investigate children, should return `true` if element is complete (does not need
     *                walking into) and return `false` if element is not complete (needs walking into)
     */
    static <T extends PsiElement> void walkTree(@Nullable PsiElement element, Function<T, Boolean> function, Class<T> typeFilter, int maxDepth) {
        if (element != null && maxDepth > 0) {
            for (PsiElement child : element.getChildren()) {
                if (typeFilter.isInstance(child) && !function.apply(typeFilter.cast(child))) {
                    walkTree(child, function, typeFilter, maxDepth - 1);
                }
            }
            if (element instanceof PyStringLiteralExpression) {
                for (PyStringElement pyStringElement : ((PyStringLiteralExpression) element).getStringElements()) {
                    if (typeFilter.isInstance(pyStringElement) && !function.apply(typeFilter.cast(pyStringElement))) {
                        walkTree(pyStringElement, function, typeFilter, maxDepth - 1);
                    }
                }
            }
        }
    }

    static String getStringValueForValueChild(@NotNull PsiElement valueChild) {
        return getStringValueForValueChild(valueChild, () -> PyResolveContext.defaultContext().withTypeEvalContext(TypeEvalContext.codeAnalysis(valueChild.getContainingFile().getProject(), valueChild.getContainingFile())));
    }


    static String getStringValueForValueChild(@NotNull PsiElement valueChild, Supplier<PyResolveContext> contextSupplier) {
        if (valueChild instanceof PyStringLiteralExpressionImpl) {
            return ((PyStringLiteralExpressionImpl) valueChild).getStringValue();
        } else if (valueChild instanceof PyStringElement) {
            TextRange contentRange = ((PyStringElement) valueChild).getContentRange();
            return valueChild.getText().substring(contentRange.getStartOffset(), contentRange.getEndOffset());
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
                        return formattedName.replaceAll("%s", OdooModelUtil.NAME_WILDCARD_MARKER);
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
}
