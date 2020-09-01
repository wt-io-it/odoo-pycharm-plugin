package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.index.OdooModelDefinition;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OdooModelImpl implements OdooModel {
    private final String name;
    private final Collection<VirtualFile> definingFiles;
    private final Project project;
    // TODO maybe get rid of this "cache"
    private PsiElement element;

    public OdooModelImpl(String modelName, Collection<VirtualFile> files, Project project) {
        name = modelName;
        definingFiles = files;
        this.project = project;
    }

    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public PsiElement getDefiningElement() {
        if (element == null) {
            ApplicationManager.getApplication().runReadAction(() -> {
                if (definingFiles.size() == 1) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(definingFiles.iterator().next());
                    retrieveDefiningElementFromFile(psiFile);
                } else {
                    PsiManager psiManager = PsiManager.getInstance(project);
                    OdooModuleService moduleService = OdooModuleService.getInstance(project);
                    PsiFile psiFile = definingFiles.stream()
                            .filter(file -> moduleService.getModule(file).equals(getBaseModule()))
                            .map(psiManager::findFile)
                            .findFirst()
                            .orElse(null);
                    retrieveDefiningElementFromFile(psiFile);
                }
            });
        }
        return element;
    }

    private void retrieveDefiningElementFromFile(PsiFile psiFile) {
        if (psiFile != null) {
            for (PsiElement pyline : psiFile.getChildren()) {
                if (OdooModelService.isOdooModelDefinition(pyline)) {
                    OdooModelDefinition model = new OdooModelDefinition((PyClass) pyline);
                    if (model.getName().equals(name)) {
                        element = pyline;
                        return;
                    }
                }
            }
        }
    }

    @Override
    public OdooModule getBaseModule() {
        OdooModuleService moduleService = OdooModuleService.getInstance(project);
        List<OdooModule> modules = definingFiles.stream().map(moduleService::getModule)
                .collect(Collectors.toList());
        return WithinProject.call(project, () -> modules.stream()
                .filter(module -> modules.stream().noneMatch(module::dependsOn))
                .findFirst()
                .orElse(null));
    }

    @Override
    public Set<OdooModule> getModules() {
        OdooModuleService moduleService = OdooModuleService.getInstance(project);
        return definingFiles.stream().map(moduleService::getModule)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof OdooModelImpl) {
            if (((OdooModelImpl) o).getDefiningElement().equals(getDefiningElement())) {
                String oName = ((OdooModelImpl) o).getName();
                if (oName != null) {
                    return oName.equals(getName());
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDefiningElement(), getName());
    }
}
