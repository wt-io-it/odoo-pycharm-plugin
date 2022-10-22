package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.models.index.OdooModelIE;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.Disposable;
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
        if (psiElement instanceof PyClass && OdooModelPsiElementMatcherUtil.isOdooModelDefinition(psiElement)) {
            String modelName = new OdooModelIE((PyClass) psiElement).getName();
            if (modelName != null) {
                if (treeStructure instanceof PyTypeHierarchyTreeStructure) {
                    return new OdooModelTypeHierarchyTreeStructure((PyClass) psiElement);
                }
                if (treeStructure instanceof PySubTypesHierarchyTreeStructure) {
                    return new OdooModelSubTypesHierarchyTreeStructure(psiElement, treeStructure.getBaseDescriptor());
                }
            }
        }
        return treeStructure;
    }
}
