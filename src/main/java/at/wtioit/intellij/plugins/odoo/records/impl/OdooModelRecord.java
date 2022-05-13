package at.wtioit.intellij.plugins.odoo.records.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModelRecord implements OdooRecord {
    private final OdooModel model;

    public OdooModelRecord(OdooModel model) {
        this.model = model;
    }

    @Override
    public @NotNull String getId() {
        return null;
    }

    @Override
    public @Nullable String getXmlId() {
        return null;
    }

    @Override
    public @NotNull String getPath() {
        return null;
    }

    @Override
    public @NotNull String getModelName() {
        return null;
    }

    @Override
    public PsiElement getDefiningElement() {
        return model.getDefiningElement();
    }
}