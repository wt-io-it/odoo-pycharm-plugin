package at.wtioit.intellij.plugins.odoo.models;

import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.*;
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
            @NotNull PyClass[] superClasses = new PyClass[0];
            if (!DumbService.isDumb(pyline.getProject())) {
                try {
                    superClasses = pyClass.getSuperClasses(typeEvalContext);
                    for (PyClass superClass : superClasses) {
                        if (isOdooModelName(superClass.getQualifiedName(), pyline)) {
                            return true;
                        }
                    }
                } catch (IndexNotReadyException e) {
                    superClasses = new PyClass[0];
                }
            }
            if (superClasses.length == 0) {
                // when we cannot resolve super classes we resort to string matching
                for (@NotNull PsiElement superClassElement : pyClass.getChildren()[0].getChildren()) {
                    if (superClassElement instanceof PyReferenceExpression) {
                        // TODO check classes defined in same file (make website.published.multi.mixin work)
                        if (isOdooModelName(superClassElement.getText(), pyline)) {
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
                if (importSource != null && importSource.getText().equals("odoo")
                        && PsiElementsUtil.findChildrenByClassAndName(fromImport, PyImportElement.class, "models") != null) {
                    if (ODOO_MODEL_BASE_CLASS_NAMES.contains("odoo." + name)) {
                        return true;
                    }
                } else if (importSource != null && importSource.getText().equals("odoo.models")
                        && PsiElementsUtil.findChildrenByClassAndName(fromImport, PyImportElement.class, name) != null) {
                    if (ODOO_MODEL_BASE_CLASS_NAMES.contains("odoo.models." + name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
