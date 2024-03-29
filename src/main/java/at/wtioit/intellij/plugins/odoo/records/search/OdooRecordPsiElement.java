package at.wtioit.intellij.plugins.odoo.records.search;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.icons.OdooPluginIcons;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexSubKeys;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import at.wtioit.intellij.plugins.odoo.search.AbstractOdooPsiElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
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
    public OdooIndexSubKeys getSubIndexKey() {
        return OdooIndexSubKeys.ODOO_RECORDS;
    }

    @Override
    public Icon getIcon(int flags) {
        if (icon == null) {
            WithinProject.run(getProject(), () -> {
                icon = OdooPluginIcons.getOdooIconForPsiElement(record.getDefiningElement(), flags);
            });
        }
        return icon;
    }

    public Icon getIcon() {
        return getIcon(0);
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
    public @Nullable VirtualFile findVirtualFile() {
        return getDefiningElement().getContainingFile().getVirtualFile();
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
