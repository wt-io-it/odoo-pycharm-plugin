package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooManifest;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.SystemIndependent;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OdooModuleImpl implements OdooModule {

    private final PsiDirectory directory;
    private final OdooManifest manifest;
    private List<OdooModel> models = Collections.emptyList();

    OdooModuleImpl(PsiDirectory moduleDir, PsiFile manifestFile) {
        directory = moduleDir;
        manifest = OdooManifestParser.parse(manifestFile);
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

    @Override
    public Collection<OdooModule> getDependencies(){
        return manifest.getDependencies();
    }

    @Override
    public void setModels(List<OdooModel> models) {
        this.models = models;
    }

    @Override
    public List<OdooModel> getModels() {
        return models;
    }

    @Override
    public boolean dependsOn(OdooModule module) {
        for (OdooModule dependency : this.getDependencies()) {
            if (dependency.getName().equals(module.getName())) {
                return true;
            } else if (dependency.dependsOn(module)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OdooModuleImpl) {
            return directory.equals(((OdooModuleImpl) obj).directory);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return directory.hashCode();
    }
}
