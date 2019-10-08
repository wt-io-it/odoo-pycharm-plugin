package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlToken;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static at.wtioit.intellij.plugins.odoo.PsiElementsUtil.findParent;

public class OdooGoToDeclarationHandler extends GotoDeclarationHandlerBase {
    @Override
    public @Nullable PsiElement getGotoDeclarationTarget(@Nullable PsiElement psiElement, Editor editor) {
        PyCallExpression pyCallExpression = findParent(psiElement, PyCallExpression.class, 3);
        if (pyCallExpression != null) {
            String fieldType = pyCallExpression.getCallee().getText();
            if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(fieldType)) {
                // handle fields.{N}2{M}(..., ) first argument
                return getOdooModel(psiElement);
            }
        } else {
            PyKeywordArgument pyKeywordArgument = findParent(psiElement, PyKeywordArgument.class, 2);
            pyCallExpression = findParent(psiElement, PyCallExpression.class, 4);
            if (pyKeywordArgument != null && pyCallExpression != null) {
                String fieldType = pyCallExpression.getCallee().getText();
                if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(fieldType)) {
                    String keyword = pyKeywordArgument.getKeyword();
                    if (OdooModel.ODOO_MODEL_NAME_FIELD_KEYWORD_ARGUMENTS.contains(keyword)) {
                        // handle fields.{N}2{M}(comodel_name=..., )
                        return getOdooModel(psiElement);
                    }
                }
            } else if (psiElement.getParent() instanceof PyStringLiteralExpression
                    && psiElement.getParent().getParent() instanceof PyAssignmentStatement) {
                String variableName = psiElement.getParent().getParent().getFirstChild().getText();
                if (OdooModel.ODOO_MODEL_NAME_VARIABLE_NAME.contains(variableName)) {
                    // handle _name and _inherit definitions
                    return getOdooModel(psiElement);
                }
            } else {
                PySubscriptionExpression pySubscriptionExpression = findParent(psiElement, PySubscriptionExpression.class, 2);
                if (pySubscriptionExpression != null && "env".equals(pySubscriptionExpression.getRootOperand().getName())) {
                    // handle self.env[...] and request.env[...]
                    return getOdooModel(psiElement);
                } else if (psiElement instanceof XmlToken
                        && psiElement.getParent() instanceof XmlAttributeValue
                        && psiElement.getParent().getParent() instanceof XmlAttribute
                        && "model".equals(((XmlAttribute) psiElement.getParent().getParent()).getName())) {
                    // xml attribute model="..."
                    return getOdooModel(psiElement.getProject(), psiElement.getText());
                }
            }
        }
        return null;
    }

    @Nullable
    private PsiElement getOdooModel(@NotNull PsiElement psiElement) {
        TextRange range = ((PyStringElement) psiElement).getContentRange();
        String value = psiElement.getText().substring(range.getStartOffset(), range.getEndOffset());
        return getOdooModel(psiElement.getContainingFile().getProject(), value);
    }

    @Nullable
    private  PsiElement getOdooModel(@NotNull Project project, @NotNull String value) {
        OdooModelService modelService = ServiceManager.getService(project, OdooModelService.class);
        OdooModel model = modelService.getModel(value);
        if (model != null) {
            return model.getDefiningElement();
        }
        return null;
    }


}
