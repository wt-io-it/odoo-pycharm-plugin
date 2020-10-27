package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.records.OdooRecordService;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlToken;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        } else if (OdooModulePsiElementMatcherUtil.isOdooModulePsiElement(psiElement)) {
            if (psiElement instanceof PyStringElement) {
                return getOdooModule((PyStringElement) psiElement);
            }
        }
        return null;
    }

    private PsiElement getOdooModule(PyStringElement pyString) {
        String value = getPyStringValue(pyString);
        OdooModuleService moduleService = ServiceManager.getService(pyString.getProject(), OdooModuleService.class);
        OdooModule module = moduleService.getModule(value);
        if (module != null) {
            return WithinProject.call(pyString.getProject(), module::getNavigationItem);
        }
        return null;
    }

    @Nullable
    private PsiElement getOdooModel(@NotNull PyStringElement pyString) {
        String value = getPyStringValue(pyString);
        return getOdooModel(pyString.getContainingFile().getProject(), value);
    }

    @NotNull
    private String getPyStringValue(@NotNull PyStringElement pyString) {
        TextRange range = pyString.getContentRange();
        String value = pyString.getText().substring(range.getStartOffset(), range.getEndOffset());
        return value;
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
