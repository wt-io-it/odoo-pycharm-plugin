package at.wtioit.intellij.plugins.odoo.search;

import at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.search.OdooModelPsiElement;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.modules.search.OdooModulePsiElement;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import at.wtioit.intellij.plugins.odoo.records.OdooRecordService;
import at.wtioit.intellij.plugins.odoo.records.search.OdooRecordPsiElement;
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
        // need to use the first "free" number in 2020.3 until
        // see https://youtrack.jetbrains.com/issue/IDEA-255566
        return -1;
    }

    @Override
    public boolean showInFindResults() {
        return false;
    }

    @Override
    public void fetchElements(@NotNull String pattern, @NotNull ProgressIndicator progressIndicator, @NotNull Processor<? super OdooSEResult> consumer) {
        OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
        ApplicationManager.getApplication().runReadAction(() -> {
            moduleService.getModuleNames().forEach(moduleName -> {
                if (moduleName.startsWith(pattern)) {
                    OdooModule module = moduleService.getModule(moduleName);
                    consumer.process(new OdooModulePsiElement(module, project));
                }
            });
        });

        // TODO once we switch to indexes this should be runnable also in "dumb" mode
        if (!DumbService.isDumb(project)) {
            OdooModelService modelService = ServiceManager.getService(project, OdooModelService.class);
            ApplicationManager.getApplication().runReadAction(() -> {
                for (String modelName : modelService.getModelNames()) {
                    if (modelName != null && modelName.startsWith(pattern)) {
                        consumer.process(new OdooModelPsiElement(modelService.getModel(modelName), project));
                    }
                }
            });

            String undetectedXmlId;
            String undetectedXmlIdExpectedModule;
            if (pattern.contains(".")) {
                undetectedXmlId = pattern.replaceFirst(".*(?=\\.)", OdooModelPsiElementMatcherUtil.NULL_XML_ID_KEY);
                undetectedXmlIdExpectedModule = pattern.replaceFirst("\\..*", "");
            } else {
                undetectedXmlId = OdooModelPsiElementMatcherUtil.NULL_XML_ID_KEY + "." + pattern;
                undetectedXmlIdExpectedModule = null;
            }
            OdooRecordService recordService = ServiceManager.getService(project, OdooRecordService.class);
            ApplicationManager.getApplication().runReadAction(() -> {
                WithinProject.run(project, () -> {
                    for (String xmlId : recordService.getXmlIds()) {
                        if (xmlId != null && xmlId.startsWith(pattern)) {
                            OdooRecord record = recordService.getRecord(xmlId);
                            if (record != null) {
                                consumer.process(new OdooRecordPsiElement(record, project));
                            }
                        } else if (xmlId != null && xmlId.startsWith(undetectedXmlId)) {
                            String expectedXmlId;
                            if (undetectedXmlIdExpectedModule != null) {
                                 expectedXmlId = xmlId.replaceFirst(OdooModelPsiElementMatcherUtil.NULL_XML_ID_KEY, undetectedXmlIdExpectedModule);
                            } else {
                                expectedXmlId = xmlId;
                            }
                            OdooRecord record = recordService.getRecord(expectedXmlId);
                            if (record != null) {
                                consumer.process(new OdooRecordPsiElement(record, project));
                            }
                        }
                    }
                });
            });
        }
    }

    @Override
    public boolean processSelectedItem(@NotNull OdooSEResult selected, int modifiers, @NotNull String searchText) {
        if (selected instanceof OdooModule) {
            OdooModule module = (OdooModule) selected;
            NavigationItem navigationItem = module.getNavigationItem();
            if (navigationItem != null) {
                navigationItem.navigate(true);
                return true;
            }
        } else if (selected instanceof OdooModel) {
            OdooModel model = (OdooModel) selected;
            PsiElement definingElement = model.getDefiningElement();
            if (definingElement instanceof NavigationItem) {
                ((NavigationItem) definingElement).navigate(true);
                return true;
            }
        } else if (selected instanceof OdooRecord) {
            OdooRecord record = (OdooRecord) selected;
            PsiElement definingElement = record.getDefiningElement();
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
