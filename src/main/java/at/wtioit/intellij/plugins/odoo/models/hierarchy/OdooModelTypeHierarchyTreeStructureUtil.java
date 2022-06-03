package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.impl.OdooModelImpl;
import at.wtioit.intellij.plugins.odoo.models.index.OdooModelDefinition;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
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
                OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
                definingElements.sort(Comparator.comparing(e -> moduleName(moduleService, e)));
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

    @NotNull
    private static String moduleName(OdooModuleService moduleService, PsiElement e) {
        OdooModule module = moduleService.getModule(e.getContainingFile().getVirtualFile());
        if (module != null) {
            return module.getName();
        }
        return "";
    }

    public static Object[] buildChildren(@NotNull PsiElement psiElement, @NotNull HierarchyNodeDescriptor hierarchyNodeDescriptor, @NotNull List<Object> children) {
        children = new ArrayList<>(children);
        OdooModelService modelService = ServiceManager.getService(hierarchyNodeDescriptor.getProject(), OdooModelService.class);
        OdooModel modelForHierarchy = modelService.getModelForElement(psiElement);
        if (modelForHierarchy != null) {
            OdooModelTypeHierarchyTreeStructureUtil.addOdooModelChildren(modelForHierarchy, hierarchyNodeDescriptor, children);
        }
        return children.toArray();
    }
}
