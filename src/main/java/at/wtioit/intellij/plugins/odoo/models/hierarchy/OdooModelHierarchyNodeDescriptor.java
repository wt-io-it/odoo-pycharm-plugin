package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.icons.OdooPluginIcons;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.hierarchy.PyHierarchyNodeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdooModelHierarchyNodeDescriptor extends PyHierarchyNodeDescriptor {

    OdooModelHierarchyNodeDescriptor(NodeDescriptor parentDescriptor, @NotNull PsiElement element, boolean isBase) {
        super(parentDescriptor, element, isBase);
    }

    @Nullable
    @Override
    protected Icon getIcon(@NotNull PsiElement element) {
        return OdooPluginIcons.ODOO_TREE_ICON;
    }
}
