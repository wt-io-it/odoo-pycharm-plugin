package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyPlainStringElement;
import com.jetbrains.python.psi.PyStringElement;
import org.jetbrains.annotations.Nullable;

public class OdooGoToDeclarationHandler extends GotoDeclarationHandlerBase {
    @Override
    public @Nullable PsiElement getGotoDeclarationTarget(@Nullable PsiElement psiElement, Editor editor) {
        if (psiElement.getParent().getParent().getParent() instanceof PyCallExpression) {
            String fieldType = ((PyCallExpression) psiElement.getParent().getParent().getParent()).getCallee().getText();
            if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(fieldType)) {
                OdooModelService modelService = ServiceManager.getService(psiElement.getContainingFile().getProject(), OdooModelService.class);
                TextRange range = ((PyStringElement) psiElement).getContentRange();
                String value = psiElement.getText().substring(range.getStartOffset(), range.getEndOffset());
                return modelService.getModel(value).getDefiningElement();
            }
        }
        return null;
    }


}
