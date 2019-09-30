package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.SystemIndependent;

import javax.swing.*;

public class OdooModuleImpl implements OdooModule {

    private final PsiDirectory directory;

    OdooModuleImpl(PsiDirectory moduleDir) {
        directory = moduleDir;
    }

    @Override
    public String getName() {
        return directory.getName();
    }

    @Override
    public PsiElement getDirectory() {
        return directory;
    }

    @Override
    public Icon getIcon() {
        return directory.getIcon(0);
    }

    @Override
    public String getRelativeLocationString() {
        String locationString = directory.getPresentation().getLocationString();
        @SystemIndependent String basePath = directory.getProject().getBasePath();
        if (locationString.startsWith(basePath)) {
            return locationString.substring(basePath.length() + 1);
        }
        return locationString;
    }


}
