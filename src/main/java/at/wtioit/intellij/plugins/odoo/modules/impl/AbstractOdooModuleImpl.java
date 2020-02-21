package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.util.Collections;
import java.util.List;

public abstract class AbstractOdooModuleImpl implements OdooModule {

    private List<OdooModel> models = Collections.emptyList();

    @Override
    public @NotNull List<OdooModel> getModels() {
        return models;
    }

    @Override
    public void setModels(List<OdooModel> models) {
        this.models = models;
    }

    @Override
    public String getRelativeLocationString() {
        String locationString = getPath();
        String basePath = null;
        // TODO need to get basePath
        //@SystemIndependent String basePath = getDirectory().getProject().getBasePath();
        if (basePath != null && locationString.startsWith(basePath)) {
            return locationString.substring(basePath.length() + 1);
        }
        return locationString;
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
        if (obj instanceof AbstractOdooModuleImpl) {
            return getPath().equals(((AbstractOdooModuleImpl) obj).getPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

}
