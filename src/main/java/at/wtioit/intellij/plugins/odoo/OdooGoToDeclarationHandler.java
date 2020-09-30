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
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static at.wtioit.intellij.plugins.odoo.PsiElementsUtil.findParent;

public class OdooGoToDeclarationHandler extends GotoDeclarationHandlerBase {

    @Override
    public @Nullable PsiElement getGotoDeclarationTarget(@Nullable PsiElement psiElement, Editor editor) {
        if (OdooModelPsiElementMatcherUtil.isOdooModelPsiElement(psiElement)) {
            if (psiElement instanceof PyStringElement) {
                PyStringElement pyStringElement = (PyStringElement) psiElement;
                PyClass inClass = PsiElementsUtil.findParent(pyStringElement, PyClass.class);
                PsiElement odooModelElement = getOdooModel(pyStringElement);
                if (inClass == odooModelElement) {
                    PyAssignmentStatement assignmentStatement = findParent(pyStringElement, PyAssignmentStatement.class, 4);
                    if (assignmentStatement != null) {
                        String variableName = assignmentStatement.getFirstChild().getText();
                        if ("_name".equals(variableName)) {
                            // avoid resolving itself for _name
                            return null;
                        }
                    }
                }
                return odooModelElement;
            } else if (psiElement instanceof XmlToken) {
                return getOdooModel(psiElement.getProject(), psiElement.getText());
            }
        }
        return null;
    }

    @Nullable
    private PsiElement getOdooModel(@NotNull PyStringElement pyString) {
        TextRange range = pyString.getContentRange();
        String value = pyString.getText().substring(range.getStartOffset(), range.getEndOffset());
        return getOdooModel(pyString.getContainingFile().getProject(), value);
    }

    @Nullable
    private  PsiElement getOdooModel(@NotNull Project project, @NotNull String value) {
        OdooModelService modelService = ServiceManager.getService(project, OdooModelService.class);
        OdooModel model = modelService.getModel(value);
        if (model != null) {
            PsiElement definingElement = model.getDefiningElement();
            return definingElement;
        }
        return null;
    }


}
