package at.wtioit.intellij.plugins.odoo.search;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.search.OdooModelPsiElement;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.modules.search.OdooModulePsiElement;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.projectView.impl.ProjectViewSharedSettings;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdooSEContributor implements SearchEverywhereContributor<OdooSEResult> {

    private final Project project;

    public OdooSEContributor(Project forProject) {
        project = forProject;
    }

    @Override
    public @NotNull String getSearchProviderId() {
        return OdooSEContributor.class.getSimpleName();
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
    public void fetchElements(@NotNull String pattern, @NotNull ProgressIndicator progressIndicator, @NotNull Processor<? super OdooSEResult> consumer) {
        OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
        ApplicationManager.getApplication().runReadAction(() -> {
            for (OdooModule module : moduleService.getModules()) {
                if (module != null && module.getName().startsWith(pattern)) {
                    consumer.process(new OdooModulePsiElement(module, project));
                }
            }
        });

        // TODO once we switch to indexes this should be runnable also in "dumb" mode
        if (!DumbService.isDumb(project)) {
            OdooModelService service = ServiceManager.getService(project, OdooModelService.class);
            ApplicationManager.getApplication().runReadAction(() -> {
                for (String modelName : service.getModelNames()) {
                    if (modelName != null && modelName.startsWith(pattern)) {
                        consumer.process(new OdooModelPsiElement(service.getModel(modelName), project));
                    }
                }
            });
        }
    }

    @Override
    public boolean processSelectedItem(@NotNull OdooSEResult selected, int modifiers, @NotNull String searchText) {
        if (selected instanceof OdooModule) {
            OdooModule module = (OdooModule) selected;
            if (ProjectViewSharedSettings.Companion.getInstance().getAutoscrollFromSource()) {
                // autoscroll from source scrolls to last opened file if we open a directory
                // so we open the manifest file instead
                PsiFile file = module.getManifestFile();
                if (file != null) {
                    file.navigate(true);
                    return true;
                }
            } else {
                PsiDirectory moduleDirectory = (PsiDirectory) module.getDirectory();
                if (moduleDirectory != null) {
                    moduleDirectory.navigate(true);
                    return true;
                }
            }
        } else if (selected instanceof OdooModel) {
            OdooModel model = (OdooModel) selected;
            PsiElement definingElement = model.getDefiningElement();
            if (definingElement instanceof NavigationItem) {
                ((NavigationItem) definingElement).navigate(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull ListCellRenderer<? super OdooSEResult> getElementsRenderer() {
        return new OdooSERenderer();
    }

    @Override
    public @Nullable Object getDataForItem(@NotNull OdooSEResult element, @NotNull String dataId) {
        return null;
    }
}
