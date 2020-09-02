package at.wtioit.intellij.plugins.odoo.search;

import com.intellij.ide.util.PsiElementListCellRenderer;
import org.jetbrains.annotations.Nullable;

public class OdooSERenderer extends PsiElementListCellRenderer<OdooSEResult> {

    @Override
    public String getElementText(OdooSEResult element) {
        return element.getName();
    }

    @Override
    protected @Nullable String getContainerText(OdooSEResult element, String name) {
        // TODO shorten if necessary
        return element.getLocationString();
    }

    @Override
    protected int getIconFlags() {
        return 0;
    }

}
