package at.wtioit.intellij.plugins.odoo.search;

import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class OdooSearchEverywhere implements SearchEverywhereContributorFactory<OdooSEResult> {
    @Override
    public @NotNull SearchEverywhereContributor<OdooSEResult> createContributor(@NotNull AnActionEvent initEvent) {
        return new OdooSEContributor(initEvent.getProject());
    }
}
