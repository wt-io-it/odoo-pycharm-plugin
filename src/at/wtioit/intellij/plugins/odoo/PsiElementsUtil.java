package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface PsiElementsUtil {
    @Nullable
    static <T extends PsiElement> T findParent(PsiElement element, Class<T> parentClass) {
        return findParent(element, parentClass, 100);
    }

    @Nullable
    static <T extends PsiElement> T findParent(PsiElement element, Class<T> parentClass, int inspectionLimit) {
        PsiElement parent = element.getParent();
        for (int i = 0; parent != null && i < inspectionLimit; i++) {
            if (parentClass.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
}
