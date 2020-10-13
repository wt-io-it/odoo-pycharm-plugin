package at.wtioit.intellij.plugins.odoo.records;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OdooRecord {

    @NotNull
    String getId();

    /**
     * @return xmlId for record, may be `null` if we are in dumb indexing mode and cannot detect the module the record
     * is in.
     */
    @Nullable
    String getXmlId();

    @NotNull
    String getPath();

    @NotNull
    String getModelName();

    PsiElement getDefiningElement();
}
