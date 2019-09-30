package at.wtioit.intellij.plugins.odoo.modules;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;

import javax.swing.*;

public interface OdooModule {
    String getName();
    PsiElement getDirectory();
    Icon getIcon();
    String getRelativeLocationString();
}
