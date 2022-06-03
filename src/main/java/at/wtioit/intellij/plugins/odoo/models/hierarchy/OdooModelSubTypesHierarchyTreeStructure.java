package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.hierarchy.treestructures.PySubTypesHierarchyTreeStructure;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;


public class OdooModelSubTypesHierarchyTreeStructure extends PySubTypesHierarchyTreeStructure {

    private final PsiElement psiElement;

    public OdooModelSubTypesHierarchyTreeStructure(PsiElement psiElement, HierarchyNodeDescriptor baseDescriptor) {
        super(psiElement.getProject(), baseDescriptor);
        this.psiElement = psiElement;
    }

    @NotNull
    @Override
    protected Object[] buildChildren(@NotNull HierarchyNodeDescriptor hierarchyNodeDescriptor) {
        return OdooModelTypeHierarchyTreeStructureUtil.buildChildren(psiElement, hierarchyNodeDescriptor, Arrays.asList(super.buildChildren(hierarchyNodeDescriptor)));
    }

}
