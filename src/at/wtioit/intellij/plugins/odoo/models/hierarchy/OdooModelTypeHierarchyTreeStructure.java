package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.jetbrains.python.hierarchy.PyHierarchyNodeDescriptor;
import com.jetbrains.python.hierarchy.treestructures.PyTypeHierarchyTreeStructure;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OdooModelTypeHierarchyTreeStructure extends PyTypeHierarchyTreeStructure {

    private final OdooModel model;

    public OdooModelTypeHierarchyTreeStructure(PyClass pyClass, OdooModel model) {
        super(pyClass);
        this.model = model;
    }

    @NotNull
    @Override
    protected Object[] buildChildren(@NotNull HierarchyNodeDescriptor hierarchyNodeDescriptor) {
        List<Object> children = new ArrayList<>(Arrays.asList(super.buildChildren(hierarchyNodeDescriptor)));
        for (OdooModule module : model.getModules()) {
            for (OdooModel inheritsModel : module.getModels()) {
                if (!inheritsModel.equals(model)
                        && inheritsModel.getName() != null
                        && inheritsModel.getName().equals(model.getName())) {
                    children.add(new PyHierarchyNodeDescriptor(hierarchyNodeDescriptor, inheritsModel.getDefiningElement(), false));
                }
            }
        }
        return children.toArray();
    }
}
