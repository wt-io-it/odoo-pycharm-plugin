package at.wtioit.intellij.plugins.odoo.modules;

import com.intellij.psi.PsiElement;

import javax.swing.*;
import java.util.Collection;

public interface OdooModule {
    String getName();
    PsiElement getDirectory();
    Icon getIcon();
    String getRelativeLocationString();
    Collection<OdooModule> getDependencies();
}
