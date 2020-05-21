package at.wtioit.intellij.plugins.odoo.models.search;

import at.wtioit.intellij.plugins.odoo.OdooSearchEverywhere;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdooModelSEContributor implements SearchEverywhereContributor<OdooModel> {
    private final Project project;

    public OdooModelSEContributor(@Nullable Project forProject) {
        project = forProject;
    }

    @Override
    public @NotNull String getSearchProviderId() {
        return OdooModelSEContributor.class.getSimpleName();
    }

    @Override
    public @NotNull String getGroupName() {
        return OdooSearchEverywhere.GROUP_NAME;
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
    public void fetchElements(@NotNull String pattern, @NotNull ProgressIndicator progressIndicator, @NotNull Processor consumer) {
        if(!DumbService.isDumb(project)) {
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
    public boolean processSelectedItem(@NotNull OdooModel selected, int modifiers, @NotNull String searchText) {
        PsiElement definingElement = selected.getDefiningElement();
        if (definingElement instanceof NavigationItem) {
            ((NavigationItem) definingElement).navigate(true);
        }
        return false;
    }

    @Override
    public @NotNull ListCellRenderer getElementsRenderer() {
        return new OdooModelSERenderer();
    }

    @Override
    public @Nullable Object getDataForItem(@NotNull OdooModel element, @NotNull String dataId) {
        return null;
    }
}
