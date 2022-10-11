package at.wtioit.intellij.plugins.odoo.records.impl;

import at.wtioit.intellij.plugins.odoo.icons.OdooPluginIcons;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexSubKeys;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdooModelRecord implements OdooRecord {
    private final OdooModel model;

    public OdooModelRecord(OdooModel model) {
        this.model = model;
    }

    @Override
    public OdooIndexSubKeys getSubIndexKey() {
        return OdooIndexSubKeys.ODOO_MODELS;
    }

    @Override
    public @NotNull String getId() {
        return "model_" + model.getName().replace(".", "_");
    }

    @Override
    public @Nullable String getXmlId() {
        return model.getBaseModule().getName() + "." + getId();
    }

    @Override
    public @NotNull String getPath() {
        return null;
    }

    @Override
    public @NotNull String getModelName() {
        return "ir.model";
    }

    @Override
    public PsiElement getDefiningElement() {
        return model.getDefiningElement();
    }

    @Override
    public @Nullable VirtualFile findVirtualFile() {
        return getDefiningElement().getContainingFile().getVirtualFile();
    }

    @Override
    public @Nullable Icon getIcon() {
        return OdooPluginIcons.getOdooIconForPsiElement(model.getDefiningElement());
    }
}
