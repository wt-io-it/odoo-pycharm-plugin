package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.hierarchy.PyTypeHierarchyBrowser;
import com.jetbrains.python.hierarchy.treestructures.PySubTypesHierarchyTreeStructure;
import com.jetbrains.python.hierarchy.treestructures.PyTypeHierarchyTreeStructure;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModelTypesHierarchyBrowser extends PyTypeHierarchyBrowser implements Disposable {

    OdooModelTypesHierarchyBrowser(PyClass pyClass) {
        super(pyClass);
    }

    @Override
    protected @Nullable HierarchyTreeStructure createHierarchyTreeStructure(@NotNull String typeName, @NotNull PsiElement psiElement) {
        @Nullable HierarchyTreeStructure treeStructure = super.createHierarchyTreeStructure(typeName, psiElement);
        Project project = psiElement.getProject();
        OdooModelService modelService = ServiceManager.getService(project, OdooModelService.class);
        OdooModel model = modelService.getModelForElement(psiElement);
        if (model != null) {
            if (treeStructure instanceof PyTypeHierarchyTreeStructure) {
                return new OdooModelTypeHierarchyTreeStructure((PyClass) psiElement, model);
            }
            if (treeStructure instanceof PySubTypesHierarchyTreeStructure) {
                return new OdooModelSubTypesHierarchyTreeStructure(project, treeStructure.getBaseDescriptor(), model);
            }
        }
        return treeStructure;
    }
}
