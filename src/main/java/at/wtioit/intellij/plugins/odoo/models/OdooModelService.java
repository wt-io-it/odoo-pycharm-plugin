package at.wtioit.intellij.plugins.odoo.models;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface OdooModelService {

    static OdooModelService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, OdooModelService.class);
    }

    Iterable<OdooModel> getModels();

    OdooModel getModel(String modelName);

    Iterable<String> getModelNames();

    OdooModel getModelForElement(PsiElement psiElement);

    boolean hasModel(String name);
}
