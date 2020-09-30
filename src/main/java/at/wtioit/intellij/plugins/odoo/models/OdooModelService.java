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

    static OdooModelService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, OdooModelService.class);
    }

    Iterable<OdooModel> getModels();

    OdooModel getModel(String modelName);

    Iterable<String> getModelNames();

    OdooModel getModelForElement(PsiElement psiElement);

    boolean hasModel(String name);
}
