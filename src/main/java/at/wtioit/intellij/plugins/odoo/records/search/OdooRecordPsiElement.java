package at.wtioit.intellij.plugins.odoo.records.search;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.icons.OdooPluginIcons;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import at.wtioit.intellij.plugins.odoo.search.AbstractOdooPsiElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.ui.LayeredIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdooRecordPsiElement extends AbstractOdooPsiElement implements OdooRecord {

    final OdooRecord record;
    private LayeredIcon icon;

    public OdooRecordPsiElement(@NotNull OdooRecord record, @NotNull Project project) {
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
        String name = getXmlId();
        if (name != null) {
            return name;
        }
        return getId();
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
