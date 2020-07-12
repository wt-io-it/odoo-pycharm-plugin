package at.wtioit.intellij.plugins.odoo;

import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyFromImportStatement;
import com.jetbrains.python.psi.PyImportElement;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

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
                return (T) parent;
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
}
