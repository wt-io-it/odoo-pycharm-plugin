package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ResolveLaterOdooModuleImpl implements OdooModule {

    private final String moduleName;
    private final Project project;
    private OdooModule module;

    public ResolveLaterOdooModuleImpl(String dependencyName, Project project) {
        moduleName = dependencyName;
        this.project = project;
    }

    @NotNull
    @Override
    public String getName() {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            return moduleName;
        }
        return module.getName();
    }

    private void tryResolveOdooModule() throws FileNotFoundException {
        OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
        module = moduleService.getModule(moduleName);
        if (module == null) {
            module = moduleService.findModule(moduleName);
        }
        if (module == null) {
            // TODO missing dependency?
            throw new FileNotFoundException("Cannot find module for " + moduleName);
        }
    }

    @Override
    public @NotNull String getPath() {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            // TODO NotImplementedException?
            throw new IllegalStateException();
        }
        return module.getPath();
    }

    @Override
    public PsiElement getDirectory() {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            // TODO NotImplementedException?
            return null;
        }
        return module.getDirectory();
    }

    @Override
    public Icon getIcon() {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            // TODO custom icon for unresolved modules or NotImplementedException?
            return null;
        }
        return module.getIcon();
    }

    @Override
    public String getRelativeLocationString() {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            return null;
        }
        return module.getRelativeLocationString();
    }

    @NotNull
    @Override
    public Collection<OdooModule> getDependencies() {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        }
        return module.getDependencies();
    }

    @NotNull
    @Override
    public List<OdooModel> getModels() {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        }
        return module.getModels();
    }

    @Override
    public boolean dependsOn(OdooModule possibleDependency) {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            // TODO give a hint to the user that we cannot find the module (and he should add it to the workspace)
            return false;
        }
        return module.dependsOn(possibleDependency);
    }

    @Override
    public PsiFile getManifestFile() {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            // TODO give a hint to the user that we cannot find the module (and he should add it to the workspace)
            return null;
        }
        return module.getManifestFile();
    }

    @Override
    public @Nullable NavigatablePsiElement getNavigationItem() {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            // TODO give a hint to the user that we cannot find the module (and he should add it to the workspace)
            return null;
        }
        return module.getNavigationItem();
    }
}
