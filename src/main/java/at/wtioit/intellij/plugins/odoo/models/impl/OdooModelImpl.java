package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.index.OdooModelDefinition;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class OdooModelImpl implements OdooModel {
    private final String name;
    private final Collection<VirtualFile> definingFiles;
    private final Project project;
    private OdooModule baseModule = null;

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
        PsiElement psiElement = ApplicationManager.getApplication().runReadAction((Computable<PsiElement>) () -> {
            if (definingFiles.size() == 1) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(definingFiles.iterator().next());
                return retrieveDefiningElementFromFile(psiFile);
            } else {
                PsiManager psiManager = PsiManager.getInstance(project);
                OdooModuleService moduleService = OdooModuleService.getInstance(project);
                PsiFile psiFile = definingFiles.stream()
                        .map(file -> Pair.create(file, moduleService.getModule(file)))
                        .filter(pair -> pair.first != null && pair.second != null)
                        .filter(pair -> pair.second.equals(getBaseModule()))
                        .map(pair -> pair.first)
                        .findFirst()
                        .map(psiManager::findFile)
                        .orElse(null);
                return retrieveDefiningElementFromFile(psiFile);
            }
        });
        return psiElement;
    }

    @NotNull
    public List<PsiElement> getDefiningElements() {
        return ApplicationManager.getApplication().runReadAction((Computable<List<PsiElement>>) () -> {
            PsiManager psiManager = PsiManager.getInstance(project);
            return definingFiles.stream()
                    .map(psiManager::findFile)
                    .map(this::retrieveDefiningElementFromFile)
                    .collect(Collectors.toList());
        });
    }

    private PsiElement retrieveDefiningElementFromFile(PsiFile psiFile) {
        if (psiFile != null) {
            AtomicReference<PsiElement> definingElement = new AtomicReference<>();
            PsiElementsUtil.walkTree(psiFile, (child) -> {
                if (definingElement.get() != null) return true;
                if (OdooModelPsiElementMatcherUtil.isOdooModelDefinition(child)) {
                    OdooModelDefinition model = new OdooModelDefinition((PyClass) child);
                    if (model.getName().equals(this.name)) {
                        definingElement.set(child);
                        return true;
                    }
                }
                return false;
            });
            return definingElement.get();
        }
        return null;
    }

    @Override
    public OdooModule getBaseModule() {
        if (baseModule == null) {
            OdooModuleService moduleService = OdooModuleService.getInstance(project);
            List<OdooModule> modules = definingFiles.stream().map(moduleService::getModule)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            baseModule = WithinProject.call(project, () ->
                    modules.stream()
                            // if base module defines the model its the base module
                            .filter(module -> "base".equals(module.getName()))
                            .findFirst()
                            .orElse(
                                    modules.stream()
                                            .filter(module -> modules.stream().noneMatch(module::dependsOn))
                                            .findFirst()
                                            .orElse(null)
                            )
                    );
        }
        return baseModule;
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
