package at.wtioit.intellij.plugins.odoo.modules.search;

import com.intellij.ide.util.PsiElementListCellRenderer;
import org.jetbrains.annotations.Nullable;

public class OdooModuleSERenderer extends PsiElementListCellRenderer<OdooModulePsiElement> {

    @Override
    public String getElementText(OdooModulePsiElement element) {
        return element.getName();
    }

    @Override
    protected @Nullable String getContainerText(OdooModulePsiElement element, String name) {
        // TODO shorten if necessary
        return element.getRelativeLocationString();
    }

    @Override
    protected int getIconFlags() {
        return 0;
    }

}
