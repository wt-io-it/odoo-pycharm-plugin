package at.wtioit.intellij.plugins.odoo.records.index;

import at.wtioit.intellij.plugins.odoo.models.index.OdooModelIE;
import at.wtioit.intellij.plugins.odoo.records.AbstractOdooRecord;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModelDefinitionRecord extends AbstractOdooRecord {

    public OdooModelDefinitionRecord(OdooModelIE entry, @NotNull String path) {
        super(getXmlId(entry), getXmlId(entry), "ir.model", path);
    }

    private static String getXmlId(OdooModelIE entry) {
        // TODO not all models are base.model_
        return "base.model_" + entry.getName().replace(".", "_");
    }

    @Override
    public PsiElement getDefiningElement() {
        return null;
    }

    @Override
    public @Nullable VirtualFile findVirtualFile() {
        return null;
    }
}
