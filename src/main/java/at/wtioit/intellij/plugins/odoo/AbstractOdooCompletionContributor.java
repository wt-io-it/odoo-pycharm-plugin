package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.OdooModelUtil;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import at.wtioit.intellij.plugins.odoo.records.OdooRecordService;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

abstract class AbstractOdooCompletionContributor extends CompletionContributor {

    void suggestModuleName(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result, String value) {
        OdooModuleService moduleService = parameters.getOriginalFile().getProject().getService(OdooModuleService.class);
        PsiDirectory odooDirectory = moduleService.getOdooDirectory();
        if (odooDirectory != null) {
            PrefixMatcher matcher =  result.getPrefixMatcher();

            for (OdooModule module : moduleService.getModules()) {
                if (result.isStopped()) {
                    // cancel further suggestions
                    return;
                }
                String moduleName = module.getName();
                String pythonModuleImportPath = "odoo.addons." + moduleName;
                if ((moduleName.startsWith(value) || matcher.prefixMatches(moduleName)) && !pythonModuleImportPath.equals(getModulePythonImportPath(odooDirectory, module))) {
                    PsiElement directory = module.getDirectory();
                    if (directory != null) {
                        LookupElementBuilder element = LookupElementBuilder
                                .createWithSmartPointer(moduleName, directory)
                                .withIcon(module.getIcon())
                                .withTailText(" " + module.getRelativeLocationString(), true);
                        // TODO add insert handler if used in code (not import statement)?
                        result.addElement(element);
                    }
                }
            }
        }
    }

    private String getModulePythonImportPath(PsiDirectory odooDirectory, OdooModule module) {
        try {
            WithinProject.INSTANCE.set(odooDirectory.getProject());
            PsiDirectory directory = (PsiDirectory) module.getDirectory();
            ArrayList<PsiDirectory> directories = new ArrayList<>();
            while (directory != null && directory != odooDirectory) {
                directories.add(directory);
                directory = directory.getParent();
            }
            StringBuilder path = new StringBuilder();
            if (directory == odooDirectory) {
                Collections.reverse(directories);
                for (PsiDirectory pathDirectory : directories) {
                    if (path.length() > 0) {
                        path.append('.');
                    }
                    path.append(pathDirectory.getName());
                }
            }
            return path.toString();
        } finally {
            WithinProject.INSTANCE.remove();
        }
    }

    void suggestModelName(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result, String value) {
        Project project = parameters.getOriginalFile().getProject();
        OdooModelService modelService = project.getService(OdooModelService.class);
        PrefixMatcher matcher =  result.getPrefixMatcher();

        for (String modelName : modelService.getModelNames()) {
            if (result.isStopped()) {
                // cancel further suggestions
                return;
            }
            if (modelName != null && (modelName.startsWith(value) || matcher.prefixMatches(modelName))) {
                OdooModule module = modelService.getModel(modelName).getBaseModule();
                if (module != null) {
                    WithinProject.run(project, () -> {
                        PsiElement directory = module.getDirectory();
                        if (directory != null) {
                            LookupElementBuilder element = LookupElementBuilder
                                    .createWithSmartPointer(OdooModelUtil.removeWildcards(modelName), directory)
                                    .withIcon(module.getIcon())
                                    .withTailText(" " + module.getRelativeLocationString(), true);
                            result.addElement(element);
                        }
                    });
                }
            }
        }
    }

    void suggestRecordXmlId(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result, String value) {
        OdooRecordService recordService = parameters.getOriginalFile().getProject().getService(OdooRecordService.class);
        PrefixMatcher matcher =  result.getPrefixMatcher();

        for (String xmlId : recordService.getXmlIds()) {
            if (result.isStopped()) {
                // cancel further suggestions
                return;
            }
            if (xmlId.startsWith(value) || matcher.prefixMatches(xmlId)) {
                // TODO if we can know the model (e.g. field, only suggest records with matching model)
                OdooRecord record = recordService.getRecord(xmlId);
                WithinProject.run(parameters.getOriginalFile().getProject(), () -> {
                    PsiElement definingElement = record.getDefiningElement();
                    if (definingElement != null) {
                        LookupElementBuilder element = LookupElementBuilder
                                .createWithSmartPointer(xmlId, definingElement)
                                .withIcon(record.getIcon())
                                .withTailText(" " + record.getPath(), true);
                        result.addElement(element);
                    }
                });
            }
        }
    }
}
