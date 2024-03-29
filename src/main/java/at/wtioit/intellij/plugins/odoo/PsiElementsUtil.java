package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.index.IndexWatcher;
import at.wtioit.intellij.plugins.odoo.models.OdooModelUtil;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PsiElementsUtil {

    Logger logger = Logger.getInstance(PsiElementsUtil.class);

    enum TREE_WALING_SIGNAL {
        INVESTIGATE_CHILDREN,
        SKIP_CHILDREN;

        public static TREE_WALING_SIGNAL should_skip(boolean b) {
            return b ? SKIP_CHILDREN : INVESTIGATE_CHILDREN;
        }

        public static TREE_WALING_SIGNAL investigate(boolean b) {
            return b ? INVESTIGATE_CHILDREN : SKIP_CHILDREN;
        }
    }

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
    static <T extends PsiElement> T getPrevSibling(@NotNull PsiElement element, Class<T> siblingClass) {
        PsiElement prevSibling = element.getPrevSibling();
        while (prevSibling != null) {
            if (siblingClass.isAssignableFrom(prevSibling.getClass())) {
                return siblingClass.cast(prevSibling);
            }
            prevSibling = prevSibling.getPrevSibling();
        }
        return null;
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
    static void walkTree(@Nullable PsiElement element, Function<PsiElement, TREE_WALING_SIGNAL> function) {
        walkTree(element, function, PsiElement.class);
    }

    /**
     * walk the PsiElement tree (downwards)
     * @param element element to walk into
     * @param function function to investigate children, should return `true` if element is complete (does not need
     *                walking into) and return `false` if element is not complete (needs walking into)
     */
    static <T extends PsiElement> void walkTree(@Nullable PsiElement element, Function<T, TREE_WALING_SIGNAL> function, Class<T> typeFilter) {
        walkTree(element, function, typeFilter, Integer.MAX_VALUE);
    }

    /**
     * walk the PsiElement tree (downwards)
     * @param element element to walk into
     * @param function function to investigate children, should return `true` if element is complete (does not need
     *                walking into) and return `false` if element is not complete (needs walking into)
     */
    static <T extends PsiElement> void walkTree(@Nullable PsiElement element, Function<T, TREE_WALING_SIGNAL> function, Class<T> typeFilter, int maxDepth) {
        if (element != null && maxDepth > 0) {
            for (PsiElement child : element.getChildren()) {
                if (typeFilter.isInstance(child) && function.apply(typeFilter.cast(child)) == TREE_WALING_SIGNAL.INVESTIGATE_CHILDREN) {
                    walkTree(child, function, typeFilter, maxDepth - 1);
                }
            }
            if (element instanceof PyStringLiteralExpression) {
                for (PyStringElement pyStringElement : ((PyStringLiteralExpression) element).getStringElements()) {
                    if (typeFilter.isInstance(pyStringElement) && function.apply(typeFilter.cast(pyStringElement)) == TREE_WALING_SIGNAL.INVESTIGATE_CHILDREN) {
                        walkTree(pyStringElement, function, typeFilter, maxDepth - 1);
                    }
                }
            }
        }
    }

    static String getStringValueForValueChild(@NotNull PsiElement valueChild) {
        return getStringValueForValueChild(valueChild, () -> PyResolveContext.defaultContext(TypeEvalContext.codeAnalysis(valueChild.getContainingFile().getProject(), valueChild.getContainingFile())));
    }


    static String getStringValueForValueChild(@NotNull PsiElement valueChild, Supplier<PyResolveContext> contextSupplier) {
        if (valueChild instanceof PyNoneLiteralExpression) {
            // _name = None, see src/test/resources/odoo/addons/module_indexing_special_cases/models/abstract_model.py
            return null;
        } else if (valueChild instanceof PyStringLiteralExpression) {
            PsiElement[] children = valueChild.getChildren();
            if (children.length == 1) {
                return getStringValueForValueChild(children[0], contextSupplier);
            }
            return ((PyStringLiteralExpression) valueChild).getStringValue();
        } else if (valueChild instanceof PyFormattedStringElement) {
            TextRange contentRange = ((PyStringElement) valueChild).getContentRange();
            String value = valueChild.getText().substring(contentRange.getStartOffset(), contentRange.getEndOffset());
            List<PyFStringFragment> fragments = ((PyFormattedStringElement) valueChild).getFragments();
            for (PyFStringFragment fragment : fragments) {
                // TODO try to resolve value of fragment expression
                value = value.replace(fragment.getText(), OdooModelUtil.NAME_WILDCARD_MARKER);
            }
            return value;
        } else if (valueChild instanceof PyStringElement) {
            TextRange contentRange = ((PyStringElement) valueChild).getContentRange();
            return valueChild.getText().substring(contentRange.getStartOffset(), contentRange.getEndOffset());
        } else if (valueChild instanceof PyListLiteralExpression
                || valueChild instanceof PySetLiteralExpression
                || valueChild instanceof PyTupleExpression) {
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
            // like we do for superclasses at/wtioit/intellij/plugins/odoo/OdooModelPsiElementMatcherUtil.java:206
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
        } else if (valueChild instanceof PyParenthesizedExpression) {
            // for values in parentheses we return the value for the contained expression
            // which should be correct at least for non lists
            // https://docs.python.org/3/reference/expressions.html#parenthesized-forms
            PyExpression containedExpression = ((PyParenthesizedExpression) valueChild).getContainedExpression();
            if (containedExpression != null) {
                return getStringValueForValueChild(containedExpression, contextSupplier);
            }
        } else if (valueChild instanceof PyDictLiteralExpression) {
            //firstChild() somehow returns the bracket
            PsiElement firstChild = valueChild.getChildren()[0];
            if (firstChild instanceof PyKeyValueExpression) {
                return getStringValueForValueChild(((PyKeyValueExpression) firstChild).getKey(), contextSupplier);
            }
        } else if (valueChild instanceof PyNumericLiteralExpression) {
            return valueChild.getText();
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
}
