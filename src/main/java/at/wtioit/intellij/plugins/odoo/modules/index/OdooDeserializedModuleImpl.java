package at.wtioit.intellij.plugins.odoo.modules.index;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.icons.OdooPluginIcons;
import at.wtioit.intellij.plugins.odoo.modules.OdooManifest;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.modules.impl.AbstractOdooModuleImpl;
import at.wtioit.intellij.plugins.odoo.modules.impl.OdooManifestParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.LayeredIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public class OdooDeserializedModuleImpl extends AbstractOdooModuleImpl {
    private final String name;
    private final String path;
    private PsiElement directory = null;
    private OdooManifest manifest = null;
    private Icon icon = null;

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
            if (icon == null) {
                icon = new LayeredIcon(directory.getIcon(0), OdooPluginIcons.ODOO_OVERLAY_ICON);
            }
            return icon;
        }
        return OdooPluginIcons.ODOO_TREE_ICON;
    }

    @Override
    public @NotNull Collection<OdooModule> getDependencies() {
        if (manifest == null) {
            ApplicationManager.getApplication().runReadAction(() -> {
                PsiDirectory directory = (PsiDirectory) getDirectory();
                if (directory != null) {
                    for (@NotNull PsiElement file : directory.getChildren()) {
                        if (file instanceof PsiFile && "__manifest__.py".equals(((PsiFile) file).getName())) {
                            manifest = OdooManifestParser.parse((PsiFile) file);
                        }
                    }
                } // TODO else log error
            });
        }
        return manifest.getDependencies();
    }
}
