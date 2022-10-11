package at.wtioit.intellij.plugins.odoo.icons;

import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.ui.LayeredIcon;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public interface OdooPluginIcons {
    Icon ODOO_TREE_ICON = IconLoader.getIcon("/icons/odoo_logo_o.png", OdooPluginIcons.class);
    Icon ODOO_OVERLAY_ICON = IconLoader.getIcon("/icons/odoo_logo_o_overlay.png", OdooPluginIcons.class);

    static @Nullable LayeredIcon getOdooIconForPsiElement(@Nullable PsiElement element, int flags) {
        if (element != null) {
            Icon baseIcon = element.getIcon(flags);
            if (baseIcon != null) {
                return new LayeredIcon((Icon) baseIcon, OdooPluginIcons.ODOO_OVERLAY_ICON);
            }
        }
        return null;
    }

    static @Nullable LayeredIcon getOdooIconForPsiElement(@Nullable PsiElement element) {
        return getOdooIconForPsiElement(element, 0);
    }
}
