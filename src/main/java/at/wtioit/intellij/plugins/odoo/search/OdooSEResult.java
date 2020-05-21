package at.wtioit.intellij.plugins.odoo.search;

import com.intellij.psi.PsiElement;

public interface OdooSEResult extends PsiElement {
    String getName();

    String getLocationString();
}
