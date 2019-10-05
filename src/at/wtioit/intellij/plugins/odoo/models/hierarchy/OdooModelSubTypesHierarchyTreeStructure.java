package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.hierarchy.treestructures.PySubTypesHierarchyTreeStructure;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OdooModelSubTypesHierarchyTreeStructure extends PySubTypesHierarchyTreeStructure {

    private final OdooModel model;

    OdooModelSubTypesHierarchyTreeStructure(Project project, HierarchyNodeDescriptor baseDescriptor, OdooModel model) {
        super(project, baseDescriptor);
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
