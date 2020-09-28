package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class AbstractOdooModuleImpl implements OdooModule {

    @Override
    public @NotNull List<OdooModel> getModels() {
        Iterable<OdooModel> models = ServiceManager.getService(WithinProject.INSTANCE.get(), OdooModelService.class).getModels();
        return StreamSupport.stream(models.spliterator(), true)
                .filter(model -> model.getModules().contains(this))
                .collect(Collectors.toList());
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
    public boolean dependsOn(@NotNull OdooModule module) {
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
