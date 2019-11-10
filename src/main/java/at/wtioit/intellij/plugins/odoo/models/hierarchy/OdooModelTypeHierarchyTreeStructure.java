package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.jetbrains.python.hierarchy.treestructures.PyTypeHierarchyTreeStructure;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OdooModelTypeHierarchyTreeStructure extends PyTypeHierarchyTreeStructure {

    private final OdooModel model;

    OdooModelTypeHierarchyTreeStructure(PyClass pyClass, OdooModel model) {
        super(pyClass);
        this.model = model;
    }

    @NotNull
    @Override
    protected Object[] buildChildren(@NotNull HierarchyNodeDescriptor hierarchyNodeDescriptor) {
        List<Object> children = new ArrayList<>(Arrays.asList(super.buildChildren(hierarchyNodeDescriptor)));
        OdooModelTypeHierarchyTreeStructureUtil.addOdooModelChildren(model, hierarchyNodeDescriptor, children);
        return children.toArray();
    }
}
