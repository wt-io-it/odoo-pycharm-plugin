package at.wtioit.intellij.plugins.odoo.models;

import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyFromImportStatementImpl;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface OdooModelService {
    Set<String> ODOO_MODEL_BASE_CLASS_NAMES = new HashSet<>(Arrays.asList("odoo.models.Model", "odoo.models.BaseModel",
            "odoo.models.TransientModel", "odoo.models.AbstractModel"));

    static OdooModelService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, OdooModelService.class);
    }

    Iterable<OdooModel> getModels();

    OdooModel getModel(String modelName);

    Iterable<String> getModelNames();

    OdooModel getModelForElement(PsiElement psiElement);

    static boolean isOdooModelDefinition(PsiElement pyline) {
        if (pyline instanceof PyClass) {
            TypeEvalContext typeEvalContext = TypeEvalContext.codeAnalysis(pyline.getContainingFile().getProject(), pyline.getContainingFile());
            PyClass pyClass = (PyClass) pyline;
            @NotNull PyClass[] superClasses = pyClass.getSuperClasses(typeEvalContext);
            for (PyClass superClass : superClasses) {
                if (isOdooModelName(superClass.getQualifiedName(), pyline)) {
                    return true;
                }
            }
            if (superClasses.length == 0) {
                // when we cannot resolve super classes we resort to string matching
                for (@NotNull PsiElement superClassElement : pyClass.getChildren()[0].getChildren()) {
                    if (superClassElement instanceof PyReferenceExpression) {
                        if (isOdooModelName(superClassElement.getText(), pyline)) {
                            // TODO we should check that there is an import in the file from odoo import models
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    static boolean isOdooModelName(String name, PsiElement pyline) {
        if (ODOO_MODEL_BASE_CLASS_NAMES.contains(name)) {
            return true;
        }
        PsiFile file = pyline.getContainingFile();
        if (file instanceof PyFile) {
            for (PyFromImportStatement fromImport : ((PyFile) pyline.getContainingFile()).getFromImports()) {
                PyReferenceExpression importSource = fromImport.getImportSource();
                if (importSource != null && importSource.getText().equals("odoo") && PsiElementsUtil.findChildrenByClassAndName(fromImport, PyImportElement.class, "model") != null) {
                    if (ODOO_MODEL_BASE_CLASS_NAMES.contains("odoo." + name)) {
                        return true;
                    }
                } else if (importSource != null && importSource.getText().equals("odoo.models") && PsiElementsUtil.findChildrenByClassAndName(fromImport, PyImportElement.class, name) != null) {
                    if (ODOO_MODEL_BASE_CLASS_NAMES.contains("odoo.models." + name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
