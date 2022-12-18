package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexSubKeys;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.ide.projectView.impl.ProjectViewSharedSettings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class AbstractOdooModuleImpl implements OdooModule {

    @Override
    public OdooIndexSubKeys getSubIndexKey() {
        return OdooIndexSubKeys.ODOO_MODULES;
    }

    @Override
    public @NotNull List<OdooModel> getModels() {
        Iterable<OdooModel> models = WithinProject.INSTANCE.get().getService(OdooModelService.class).getModels();
        // if we run this in parallel the UI freezes
        return StreamSupport.stream(models.spliterator(), false)
                .filter(model -> model.getModules().contains(this))
                .collect(Collectors.toList());
    }

    @Override
    public String getRelativeLocationString() {
        String locationString = getPath();
        String basePath = null;
        Project project = WithinProject.INSTANCE.get();
        if (project != null) {
            basePath = project.getBasePath();
        }
        if (basePath != null && locationString.startsWith(basePath) && basePath.length() < locationString.length()) {
            return locationString.substring(basePath.length() + 1);
        }
        return locationString;
    }

    @Override
    public boolean dependsOn(@NotNull OdooModule module) {
        String moduleName = module.getName();
        HashSet<OdooModule> checkedDependencies = new HashSet<>();
        Stack<OdooModule> dependenciesToCheck = new Stack<>();
        dependenciesToCheck.addAll(this.getDependencies());
        while (!dependenciesToCheck.empty()) {
            OdooModule dependency = dependenciesToCheck.pop();
            if (moduleName.equals(dependency.getName())) {
                return true;
            } else {
                checkedDependencies.add(dependency);
                for (OdooModule transistiveDependency: dependency.getDependencies()) {
                    if (!checkedDependencies.contains(transistiveDependency)) {
                        dependenciesToCheck.push(transistiveDependency);
                    }
                }
            }

        }
        return false;
    }

    @Override
    public @Nullable NavigatablePsiElement getNavigationItem() {
        if (ProjectViewSharedSettings.Companion.getInstance().getAutoscrollFromSource()) {
            // autoscroll from source scrolls to last opened file if we open a directory
            // so we open the manifest file instead
            return getManifestFile();
        } else {
            return (PsiDirectory) getDirectory();
        }
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
