package at.wtioit.intellij.plugins.odoo;

import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyFromImportStatement;
import com.jetbrains.python.psi.PyStringElement;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public interface PsiElementsUtil {
    @Nullable
    static <T extends PsiElement> T findParent(@Nullable PsiElement element, Class<T> parentClass) {
        return findParent(element, parentClass, 100);
    }

    @Nullable
    static <T extends PsiElement> T findParent(@Nullable PsiElement element, Class<T> parentClass, int inspectionLimit) {
        if (element == null) return null;
        PsiElement parent = element.getParent();
        for (int i = 0; parent != null && i < inspectionLimit; i++) {
            if (parentClass.isAssignableFrom(parent.getClass())) {
                return parentClass.cast(parent);
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
    static void walkTree(PsiElement element, Function<PsiElement, Boolean> function) {
        for (PsiElement child : element.getChildren()) {
            if (!function.apply(child)) {
                walkTree(child, function);
            }
        }
        if (element instanceof PyStringLiteralExpression) {
            for (PyStringElement pyStringElement : ((PyStringLiteralExpression) element).getStringElements()) {
                if (!function.apply(pyStringElement)) {
                    walkTree(pyStringElement, function);
                }
            }
        }
    }
}
