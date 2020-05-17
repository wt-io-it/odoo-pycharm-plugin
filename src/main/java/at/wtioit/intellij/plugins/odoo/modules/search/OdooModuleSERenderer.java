package at.wtioit.intellij.plugins.odoo.modules.search;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.ide.actions.SearchEverywherePsiRenderer;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.SimpleListCellRenderer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

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
