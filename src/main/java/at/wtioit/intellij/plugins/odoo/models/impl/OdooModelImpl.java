package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.OdooPluginError;
import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexSubKeys;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.index.OdooModelIE;
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
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
                List<PsiElement> psiElements = retrieveDefiningElementsFromFile(psiFile);
                return getDefiningElement(psiElements);
            } else {
                PsiManager psiManager = PsiManager.getInstance(project);
                OdooModuleService moduleService = project.getService(OdooModuleService.class);
                List<PsiFile> psiFiles = definingFiles.stream()
                        .map(file -> Pair.create(file, moduleService.getModule(file)))
                        .filter(pair -> pair.first != null && pair.second != null)
                        .filter(pair -> pair.second.equals(getBaseModule()))
                        .map(pair -> pair.first)
                        .map(psiManager::findFile)
                        .collect(Collectors.toList());
                List<PsiElement> psiElements = psiFiles.stream().map(this::retrieveDefiningElementsFromFile).collect(ArrayList::new, List::addAll, List::addAll);
                return getDefiningElement(psiElements);
            }
        });
        return psiElement;
    }

    private PsiElement getDefiningElement(List<PsiElement> psiElements) {
        AtomicReference<PyClass> definingElement = new AtomicReference<>();
        for (PsiElement element : psiElements) {
            PyClass pyClass = (PyClass) element;
            PsiElementsUtil.walkTree(pyClass, child -> {
                if (definingElement.get() != null) return PsiElementsUtil.TREE_WALING_SIGNAL.SKIP_CHILDREN;
                if (OdooModelPsiElementMatcherUtil.isOdooModelNameDefinitionPsiElement(child)) {
                    PyAssignmentStatement assignmentStatement = PsiElementsUtil.findParent(child, PyAssignmentStatement.class, 4);
                    if (assignmentStatement != null) {
                        String variableName = assignmentStatement.getLeftHandSideExpression().getText();
                        String modelName = PsiElementsUtil.getStringValueForValueChild(child);
                        if ("_name".equals(variableName) && name.equals(modelName)) {
                            definingElement.set(pyClass);
                        }
                    }
                    return PsiElementsUtil.TREE_WALING_SIGNAL.SKIP_CHILDREN;
                }
                return PsiElementsUtil.TREE_WALING_SIGNAL.INVESTIGATE_CHILDREN;
            });
        }
        if (definingElement.get() != null) {
            return definingElement.get();
        } else {
            return psiElements.stream().findFirst().orElseThrow(() -> new OdooPluginError("Cannot find defining elements for " + name + " with files " + definingFiles));
        }
    }

    @NotNull
    public List<PsiElement> getDefiningElements() {
        return ApplicationManager.getApplication().runReadAction((Computable<List<PsiElement>>) () -> {
            PsiManager psiManager = PsiManager.getInstance(project);
            return definingFiles.stream()
                    .map(psiManager::findFile)
                    .map(this::retrieveDefiningElementsFromFile)
                    .collect(ArrayList::new, List::addAll, List::addAll);
        });
    }

    private List<PsiElement> retrieveDefiningElementsFromFile(PsiFile psiFile) {
        if (psiFile != null) {
            ArrayList<PsiElement> elements = new ArrayList<>();
            PsiElementsUtil.walkTree(psiFile, (child) -> {
                if (OdooModelPsiElementMatcherUtil.isOdooModelDefinition(child)) {
                    OdooModelIE model = new OdooModelIE((PyClass) child);
                    if (model.getName() != null && model.getName().equals(this.name)) {
                        elements.add(child);
                        return PsiElementsUtil.TREE_WALING_SIGNAL.SKIP_CHILDREN;
                    }
                }
                return PsiElementsUtil.TREE_WALING_SIGNAL.INVESTIGATE_CHILDREN;
            });
            return elements;
        }
        return null;
    }

    @Override
    public OdooModule getBaseModule() {
        if (baseModule == null) {
            OdooModuleService moduleService = project.getService(OdooModuleService.class);
            List<OdooModule> modules = definingFiles.stream().map(moduleService::getModule)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            baseModule = WithinProject.call(project, () ->
                    modules.stream()
                            // if base module defines the model it's the base module
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
        OdooModuleService moduleService = project.getService(OdooModuleService.class);
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

    @Override
    public OdooIndexSubKeys getSubIndexKey() {
        return OdooIndexSubKeys.ODOO_MODELS;
    }
}
