package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

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

    @Override
    public String getName() {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            return null;
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
    public PsiElement getDirectory() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getRelativeLocationString() {
        return null;
    }

    @Override
    public Collection<OdooModule> getDependencies() {
        try {
            tryResolveOdooModule();
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        }
        return module.getDependencies();
    }

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
    public void setModels(List<OdooModel> models) {

    }
}
