package at.wtioit.intellij.plugins.odoo.modules.search;

import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class OdooModuleSearchEverywhere implements SearchEverywhereContributorFactory {
    @Override
    public @NotNull SearchEverywhereContributor createContributor(@NotNull AnActionEvent initEvent) {
        return new OdooModuleSEContributor(initEvent.getProject());
    }
}
