package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.hierarchy.PyTypeHierachyProvider;
import com.jetbrains.python.hierarchy.PyTypeHierarchyBrowser;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;

public class OdooModelHierarchyProvider extends PyTypeHierachyProvider {

    @Override
    public @NotNull HierarchyBrowser createHierarchyBrowser(@NotNull PsiElement psiElement) {
        HierarchyBrowser browser =  super.createHierarchyBrowser(psiElement);
        if (browser instanceof PyTypeHierarchyBrowser) {
            Disposer.dispose((PyTypeHierarchyBrowser) browser);
            return new OdooModelTypesHierarchyBrowser((PyClass) psiElement);
        }
        return browser;
    }
}
