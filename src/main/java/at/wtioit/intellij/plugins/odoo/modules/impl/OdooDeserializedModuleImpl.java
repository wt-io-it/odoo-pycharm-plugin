package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.modules.OdooManifest;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public class OdooDeserializedModuleImpl extends AbstractOdooModuleImpl {
    private final String name;
    private final String path;
    private PsiElement directory = null;
    private OdooManifest manifest = null;

    public OdooDeserializedModuleImpl(String moduleName, String modulePath) {
        name = moduleName;
        path = modulePath;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getPath() {
        return path;
    }

    @Override
    public @Nullable PsiElement getDirectory() {
        if (directory == null) {
            OdooModuleService moduleService = ServiceManager.getService(WithinProject.INSTANCE.get(), OdooModuleService.class);
            directory = moduleService.getModuleDirectory(path);
        }
        return directory;
    }

    @Override
    public @Nullable Icon getIcon() {
        if (directory != null) {
            return directory.getIcon(0);
        }
        //TODO show fallback icon
        return null;
    }

    @Override
    public @NotNull Collection<OdooModule> getDependencies() {
        if (manifest == null) {
            PsiDirectory directory = (PsiDirectory) getDirectory();
            for (@NotNull PsiElement file : directory.getChildren()) {
                if (file instanceof PsiFile && "__manifest__.py".equals(((PsiFile) file).getName())) {
                    manifest = OdooManifestParser.parse((PsiFile) file);
                }
            }
        }
        return manifest.getDependencies();
    }
}
