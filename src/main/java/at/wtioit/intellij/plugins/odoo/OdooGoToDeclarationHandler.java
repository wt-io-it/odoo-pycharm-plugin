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
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static at.wtioit.intellij.plugins.odoo.PsiElementsUtil.findParent;

public class OdooGoToDeclarationHandler extends GotoDeclarationHandlerBase {

    private static final List<String> ODOO_MODEL_XML_ATTRIBUTE_NAMES = Arrays.asList("model", "data-oe-model");
    private static final List<String> ODOO_MODEL_XML_FIELD_ATTRIBUTE_NAMES = Arrays.asList("model", "res_model", "src_model");

    @Override
    public @Nullable PsiElement getGotoDeclarationTarget(@Nullable PsiElement psiElement, Editor editor) {
        if (psiElement instanceof PyStringElement) {
            PyCallExpression pyCallExpression = findParent(psiElement, PyCallExpression.class, 3);
            if (pyCallExpression != null && pyCallExpression.getCallee() != null) {
                String fieldType = pyCallExpression.getCallee().getText();
                if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(fieldType)) {
                    // handle fields.{N}2{M}(..., ) first argument
                    return getOdooModel((PyStringElement) psiElement);
                }
            } else {
                PyKeywordArgument pyKeywordArgument = findParent(psiElement, PyKeywordArgument.class, 2);
                pyCallExpression = findParent(psiElement, PyCallExpression.class, 4);
                if (pyKeywordArgument != null && pyCallExpression != null && pyCallExpression.getCallee() != null) {
                    String fieldType = pyCallExpression.getCallee().getText();
                    if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(fieldType)) {
                        String keyword = pyKeywordArgument.getKeyword();
                        if (OdooModel.ODOO_MODEL_NAME_FIELD_KEYWORD_ARGUMENTS.contains(keyword)) {
                            // handle fields.{N}2{M}(comodel_name=..., )
                            return getOdooModel((PyStringElement) psiElement);
                        }
                    }
                } else {
                    PyAssignmentStatement assignmentStatement = findParent(psiElement, PyAssignmentStatement.class, 3);
                    if (assignmentStatement != null && psiElement.getParent() instanceof PyStringLiteralExpression) {
                        String variableName = assignmentStatement.getFirstChild().getText();
                        if (OdooModel.ODOO_MODEL_NAME_VARIABLE_NAME.contains(variableName)) {
                            // handle _name and _inherit definitions
                            return getOdooModelNotItself((PyStringElement) psiElement);
                        }
                    }
                    PySubscriptionExpression pySubscriptionExpression = findParent(psiElement, PySubscriptionExpression.class, 2);
                    if (pySubscriptionExpression != null && "env".equals(pySubscriptionExpression.getRootOperand().getName())) {
                        // handle self.env[...] and request.env[...]
                        return getOdooModel((PyStringElement) psiElement);
                    }
                }
            }
        } else {
            XmlAttribute xmlAttribute = findParent(psiElement, XmlAttribute.class, 2);
            if (xmlAttribute != null && ODOO_MODEL_XML_ATTRIBUTE_NAMES.contains(xmlAttribute.getName())) {
                // xml attribute model="..."
                return getOdooModel(psiElement.getProject(), psiElement.getText());
            }
            XmlTag xmlTag = findParent(psiElement, XmlTag.class, 2);
            if (xmlTag != null && "field".equals(xmlTag.getName()) && ODOO_MODEL_XML_FIELD_ATTRIBUTE_NAMES.contains(xmlTag.getAttributeValue("name"))) {
                // xml <field name="model">...</field> (also res_model and src_model)
                return getOdooModel(psiElement.getProject(), psiElement.getText());
            }
        }
        return null;
    }

    @Nullable
    private PsiElement getOdooModel(@NotNull PyStringElement pyString) {
        TextRange range = pyString.getContentRange();
        String value = pyString.getText().substring(range.getStartOffset(), range.getEndOffset());
        return getOdooModel(pyString.getContainingFile().getProject(), value, null);
    }

    @Nullable
    private PsiElement getOdooModelNotItself(@NotNull PyStringElement pyString) {
        TextRange range = pyString.getContentRange();
        String value = pyString.getText().substring(range.getStartOffset(), range.getEndOffset());
        return getOdooModel(pyString.getContainingFile().getProject(), value, pyString);
    }

    @Nullable
    private  PsiElement getOdooModel(@NotNull Project project, @NotNull String value) {
        return getOdooModel(project, value, null);
    }

    @Nullable
    private  PsiElement getOdooModel(@NotNull Project project, @NotNull String value, @Nullable PsiElement self) {
        OdooModelService modelService = ServiceManager.getService(project, OdooModelService.class);
        OdooModel model = modelService.getModel(value);
        if (model != null) {
            PsiElement definingElement = model.getDefiningElement();
            // Prevent resolving to the class that the element itself is contained in
            if (definingElement != PsiElementsUtil.findParent(self, PyClass.class)) {
                return definingElement;
            }
        }
        return null;
    }


}
