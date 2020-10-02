package at.wtioit.intellij.plugins.odoo;

import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyFromImportStatement;
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
        }
    }
}
