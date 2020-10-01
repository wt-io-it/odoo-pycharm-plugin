package at.wtioit.intellij.plugins.odoo.modules.inspection;

import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyDictLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManifestInspectionSuppressor implements InspectionSuppressor {
    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        if ("PyStatementEffect".equals(toolId) && element instanceof PyDictLiteralExpression) {
            PsiFile file = PsiElementsUtil.findParent(element, PsiFile.class, 2);
            if (file != null && "__manifest__.py".equals(file.getName())) {
                // if dict is main dict in manifest we suppress the warning about the statement having no effect
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull SuppressQuickFix[] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
        return new SuppressQuickFix[0];
    }
}
