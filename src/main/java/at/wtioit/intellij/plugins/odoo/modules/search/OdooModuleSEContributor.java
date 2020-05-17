package at.wtioit.intellij.plugins.odoo.modules.search;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdooModuleSEContributor implements SearchEverywhereContributor<OdooModule> {
    private final Project project;

    public OdooModuleSEContributor(Project forProject) {
        this.project = forProject;
    }

    @Override
    public @NotNull String getSearchProviderId() {
        return OdooModuleSEContributor.class.getSimpleName();
    }

    @Override
    public @NotNull String getGroupName() {
        return "Odoo";
    }

    @Override
    public int getSortWeight() {
        return 0;
    }

    @Override
    public boolean showInFindResults() {
        return false;
    }

    @Override
    public void fetchElements(@NotNull String pattern, @NotNull ProgressIndicator progressIndicator, @NotNull Processor<? super OdooModule> consumer) {
        OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
        for (OdooModule module : moduleService.getModules()) {
            if (module != null && module.getName().startsWith(pattern)) {
                consumer.process(new OdooModulePsiElement(module, project));
            }
        }
    }

    @Override
    public boolean processSelectedItem(@NotNull OdooModule selected, int modifiers, @NotNull String searchText) {
        return false;
    }

    @Override
    public @NotNull ListCellRenderer<? super OdooModule> getElementsRenderer() {
        return new OdooModuleSERenderer();
    }

    @Override
    public @Nullable Object getDataForItem(@NotNull OdooModule element, @NotNull String dataId) {
        return null;
    }
}
