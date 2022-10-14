package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.impl.OdooModelImpl;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class OdooModelTypeHierarchyTreeStructureUtil {

    static void addOdooModelChildren(@NotNull OdooModel model, @NotNull HierarchyNodeDescriptor hierarchyNodeDescriptor, @NotNull List<Object> children) {
        if (hierarchyNodeDescriptor instanceof OdooModelHierarchyNodeDescriptor) {
            // skip listing the same classes over and over again
            return;
        }
        Project project = hierarchyNodeDescriptor.getProject();
        if (model instanceof OdooModelImpl) {

            List<PsiElement> definingElements = ((OdooModelImpl) model).getDefiningElements();
            if (project != null) {
                OdooModuleService moduleService = project.getService(OdooModuleService.class);
                OdooModule baseModule = model.getBaseModule();
                definingElements.sort((e1, e2) -> {
                    OdooModule module1 = moduleService.getModule(e1.getContainingFile().getVirtualFile());
                    OdooModule module2 = moduleService.getModule(e2.getContainingFile().getVirtualFile());
                    if (module1 == null || module2 == null) {
                        // Cannot compare if one of the modules is null
                        return 0;
                    } else if (module1.getName().equals(baseModule.getName()) && module2.getName().equals(baseModule.getName())) {
                        return comparePaths(e1, e2);
                    } else if (module1.getName().equals(baseModule.getName())) {
                        return -1;
                    } else if (module2.getName().equals(baseModule.getName())) {
                        return 1;
                    } else {
                        int moduleComparison = module1.getName().compareTo(module2.getName());
                        // TODO factor in the dependency tree?
                        if (moduleComparison != 0) return moduleComparison;

                        return comparePaths(e1, e2);
                    }
                });
            }
            for (PsiElement definingElement : definingElements) {
                if (definingElement != model.getDefiningElement()) {
                    children.add(new OdooModelHierarchyNodeDescriptor(hierarchyNodeDescriptor, definingElement, false));
                }
            }
        } else {
            WithinProject.run(project, () -> {
                for (OdooModule module : model.getModules()) {
                    for (OdooModel inheritsModel : module.getModels()) {
                        if (inheritsModel.getName() != null
                                && inheritsModel.getName().equals(model.getName())) {
                            children.add(new OdooModelHierarchyNodeDescriptor(hierarchyNodeDescriptor, inheritsModel.getDefiningElement(), false));
                        }
                    }
                }
            });
        }
    }

    private static int comparePaths(PsiElement e1, PsiElement e2) {
        // TODO prefer models in models directory?
        return e1.getContainingFile().getVirtualFile().getPath().compareTo(e2.getContainingFile().getVirtualFile().getPath());
    }

    public static Object[] buildChildren(@NotNull PsiElement psiElement, @NotNull HierarchyNodeDescriptor hierarchyNodeDescriptor, @NotNull List<Object> children) {
        children = new ArrayList<>(children);
        Project project = hierarchyNodeDescriptor.getProject();
        if (project != null) {
            OdooModelService modelService = project.getService(OdooModelService.class);
            OdooModel modelForHierarchy = modelService.getModelForElement(psiElement);
            if (modelForHierarchy != null) {
                OdooModelTypeHierarchyTreeStructureUtil.addOdooModelChildren(modelForHierarchy, hierarchyNodeDescriptor, children);
            }
        }
        return children.toArray();
    }
}
