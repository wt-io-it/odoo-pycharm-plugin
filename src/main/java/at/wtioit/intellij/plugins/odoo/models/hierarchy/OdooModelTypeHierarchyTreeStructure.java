package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.components.ServiceManager;
import com.jetbrains.python.hierarchy.treestructures.PyTypeHierarchyTreeStructure;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OdooModelTypeHierarchyTreeStructure extends PyTypeHierarchyTreeStructure {

    private final PyClass psiElement;

    public OdooModelTypeHierarchyTreeStructure(PyClass psiElement) {
        super(psiElement);
        this.psiElement = psiElement;
    }

    @NotNull
    @Override
    protected Object[] buildChildren(@NotNull HierarchyNodeDescriptor hierarchyNodeDescriptor) {
        return OdooModelTypeHierarchyTreeStructureUtil.buildChildren(psiElement, hierarchyNodeDescriptor, Arrays.asList(super.buildChildren(hierarchyNodeDescriptor)));
    }
}
