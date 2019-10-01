package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooGoToDeclarationHandler extends GotoDeclarationHandlerBase {
    @Override
    public @Nullable PsiElement getGotoDeclarationTarget(@Nullable PsiElement psiElement, Editor editor) {
        if (psiElement.getParent().getParent().getParent() instanceof PyCallExpression) {
            String fieldType = ((PyCallExpression) psiElement.getParent().getParent().getParent()).getCallee().getText();
            if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(fieldType)) {
                return getOdooModel(psiElement);
            }
        } else if (psiElement.getParent().getParent() instanceof PyKeywordArgument
                && psiElement.getParent().getParent().getParent().getParent() instanceof PyCallExpression) {
            String fieldType = ((PyCallExpression) psiElement.getParent().getParent().getParent().getParent()).getCallee().getText();
            if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(fieldType)) {
                String keyword = ((PyKeywordArgument) psiElement.getParent().getParent()).getKeyword();
                if (OdooModel.ODOO_MODEL_NAME_FIELD_KEYWORD_ARGUMENTS.contains(keyword)) {
                    return getOdooModel(psiElement);
                }
            }
        } else if (psiElement.getParent() instanceof PyStringLiteralExpression
                && psiElement.getParent().getParent() instanceof PyAssignmentStatement) {
            String variableName = psiElement.getParent().getParent().getFirstChild().getText();
            if (OdooModel.ODOO_MODEL_NAME_VARIABLE_NAME.contains(variableName)) {
                return getOdooModel(psiElement);
            }
        }
        return null;
    }

    @Nullable
    private PsiElement getOdooModel(@NotNull PsiElement psiElement) {
        OdooModelService modelService = ServiceManager.getService(psiElement.getContainingFile().getProject(), OdooModelService.class);
        TextRange range = ((PyStringElement) psiElement).getContentRange();
        String value = psiElement.getText().substring(range.getStartOffset(), range.getEndOffset());
        OdooModel model = modelService.getModel(value);
        if (model != null) {
            return model.getDefiningElement();
        }
        return null;
    }


}
