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
import java.util.concurrent.ConcurrentHashMap;

public class OdooDeserializedModuleImpl extends AbstractOdooModuleImpl {
    private final String name;
    private final String path;
    private PsiElement directory = null;
    private OdooManifest manifest = null;
    private PsiFile manifestFile = null;
    private Icon icon = null;

    private static final ConcurrentHashMap<String, OdooDeserializedModuleImpl> modules = new ConcurrentHashMap<>();

    protected OdooDeserializedModuleImpl(String moduleName, String modulePath) {
        name = moduleName;
        path = modulePath;
        String key = name + ":" + path;
        if (!modules.containsKey(key)) {
            modules.put(key, this);
        }
    }

    public static OdooModule getInstance(String name, String path) {
        String key = name + ":" + path;
        if (!modules.containsKey(key)) {
            new OdooDeserializedModuleImpl(name, path);
        }
        return modules.get(key);
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
        if (directory == null || !directory.isValid()) {
            OdooModuleService moduleService = ServiceManager.getService(WithinProject.INSTANCE.get(), OdooModuleService.class);
            directory = moduleService.getModuleDirectory(path);
        }
        return directory;
    }

    @Override
    public @Nullable Icon getIcon() {
        if (directory != null) {
            if (icon == null) {
                icon = OdooPluginIcons.getOdooIconForPsiElement(directory);
            }
            return icon;
        } // TODO else check if we can get a directory
        return OdooPluginIcons.ODOO_TREE_ICON;
    }

    @Override
    public @NotNull Collection<OdooModule> getDependencies() {
        if (manifest == null) {
            manifest = OdooManifestParser.parse(getManifestFile());
        }
        return manifest.getDependencies();
    }

    @Override
    public PsiFile getManifestFile() {
        if (manifestFile == null || !manifestFile.isValid()) {
            ApplicationManager.getApplication().runReadAction(() -> {
                PsiDirectory directory = (PsiDirectory) getDirectory();
                if (directory != null) {
                    for (@NotNull PsiElement file : directory.getChildren()) {
                        if (file instanceof PsiFile && "__manifest__.py".equals(((PsiFile) file).getName())) {
                            manifestFile = (PsiFile) file;
                        }
                    }
                } // TODO else log error
            });
        }
        return manifestFile;
    }
}
