package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.hierarchy.PyTypeHierachyProvider;
import com.jetbrains.python.hierarchy.PyTypeHierarchyBrowser;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModelHierarchyProvider extends PyTypeHierachyProvider {
    @Override
    public @Nullable PsiElement getTarget(@NotNull DataContext dataContext) {
        return super.getTarget(dataContext);
    }

    @Override
    public @NotNull HierarchyBrowser createHierarchyBrowser(@NotNull PsiElement psiElement) {
        HierarchyBrowser browser =  super.createHierarchyBrowser(psiElement);
        if (browser instanceof PyTypeHierarchyBrowser) {
            return new OdooModelTypesHierarchyBrowser((PyClass) psiElement);
        }
        return browser;
    }

    @Override
    public void browserActivated(@NotNull HierarchyBrowser hierarchyBrowser) {
        super.browserActivated(hierarchyBrowser);
    }
}
