package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.ide.projectView.impl.ProjectViewSharedSettings;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        Project project = WithinProject.INSTANCE.get();
        if (project != null) {
            basePath = project.getBasePath();
        }
        if (basePath != null && locationString.startsWith(basePath)) {
            return locationString.substring(basePath.length() + 1);
        }
        return locationString;
    }

    @Override
    public boolean dependsOn(@NotNull OdooModule module) {
        return this.getDependencies().stream()
                .anyMatch(dependency -> dependency.getName().equals(module.getName()) || dependency.dependsOn(module));
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
