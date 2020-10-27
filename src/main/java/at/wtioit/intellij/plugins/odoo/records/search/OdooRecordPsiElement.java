package at.wtioit.intellij.plugins.odoo.records.search;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.icons.OdooPluginIcons;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import at.wtioit.intellij.plugins.odoo.search.AbstractOdooPsiElement;
import at.wtioit.intellij.plugins.odoo.search.OdooSEResult;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdooRecordPsiElement extends AbstractOdooPsiElement implements OdooRecord {

    final OdooRecord record;
    private LayeredIcon icon;

    public OdooRecordPsiElement(OdooRecord record, Project project) {
        super(project, record::getDefiningElement);
        this.record = record;
    }

    @Override
    public Icon getIcon(int flags) {
        if (icon == null) {
            WithinProject.run(getProject(), () -> {
                Icon baseIcon = record.getDefiningElement().getIcon(flags);
                icon = new LayeredIcon(baseIcon, OdooPluginIcons.ODOO_OVERLAY_ICON);
            });
        }
        return icon;
    }

    @Override
    public @NotNull String getId() {
        return record.getId();
    }

    @Override
    public @Nullable String getXmlId() {
        return record.getXmlId();
    }

    @Override
    public @NotNull String getPath() {
        return record.getPath();
    }

    @Override
    public @NotNull String getModelName() {
        return record.getModelName();
    }

    @Override
    public PsiElement getDefiningElement() {
        return WithinProject.call(getProject(), record::getDefiningElement);
    }

    @Override
    public String getName() {
        return getXmlId();
    }

    @Override
    public String getLocationString() {
        // TODO unify logic with AbstractOdooModuleImpl
        String locationString = getPath();
        String basePath = getProject().getBasePath();
        if (basePath != null && locationString.startsWith(basePath)) {
            return locationString.substring(basePath.length() + 1);
        }
        return locationString;
    }
}
