package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractOdooAddonsCompletionContributor extends CompletionContributor {
    @Nullable <T extends PsiElement> T findParent(PsiElement element, Class<T> parentClass) {
        return findParent(element, parentClass, 100);
    }

    @Nullable <T extends PsiElement> T findParent(PsiElement element, Class<T> parentClass, int inspectionLimit) {
        PsiElement parent = element.getParent();
        for (int i = 0; parent != null && i < inspectionLimit; i++) {
            if (parentClass.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    void suggestModuleName(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result, String value) {
        OdooModuleService moduleService = ServiceManager.getService(parameters.getOriginalFile().getProject(), OdooModuleService.class);
        for (OdooModule module : moduleService.getModules()) {
            if (module.getName().startsWith(value)) {
                LookupElementBuilder element = LookupElementBuilder
                        .createWithSmartPointer(module.getName(), module.getDirectory())
                        .withIcon(module.getIcon())
                        .withTailText(" " + module.getRelativeLocationString(), true);
                // TODO add insert handler if used in code (not import statement)?
                result.addElement(element);
            }
        }
    }

    void suggestModelName(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result, String value) {
        OdooModelService modelService = ServiceManager.getService(parameters.getOriginalFile().getProject(), OdooModelService.class);

        for (String modelName : modelService.getModelNames()) {
            if (modelName != null && modelName.startsWith(value)) {
                for (OdooModule module : modelService.getModel(modelName).getModules()) {
                    // TODO customize path for model definition
                    LookupElementBuilder element = LookupElementBuilder
                            .createWithSmartPointer(modelName, module.getDirectory())
                            .withIcon(module.getIcon())
                            .withTailText(" " + module.getRelativeLocationString(), true);
                    result.addElement(element);
                }
            }
        }
    }
}
