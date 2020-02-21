package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooManifest;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.SystemIndependent;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OdooModuleImpl extends AbstractOdooModuleImpl {

    private final PsiDirectory directory;
    private final OdooManifest manifest;
    private List<OdooModel> models = Collections.emptyList();

    OdooModuleImpl(PsiDirectory moduleDir, PsiFile manifestFile) {
        directory = moduleDir;
        manifest = OdooManifestParser.parse(manifestFile);
    }

    @NotNull
    @Override
    public String getName() {
        return directory.getName();
    }

    @Override
    public @NotNull String getPath() {
        return directory.getText();
    }

    @Override
    public @Nullable PsiElement getDirectory() {
        return directory;
    }

    @Override
    public Icon getIcon() {
        return directory.getIcon(0);
    }

    @Override
    public String getRelativeLocationString() {
        ItemPresentation presentation = directory.getPresentation();
        if (presentation != null) {
            String locationString = presentation.getLocationString();
            if (locationString != null) {
                @SystemIndependent String basePath = directory.getProject().getBasePath();
                if (basePath != null && locationString.startsWith(basePath)) {
                    return locationString.substring(basePath.length() + 1);
                }
                return locationString;
            }
        }
        return directory.getVirtualFile().getCanonicalPath();
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
}
