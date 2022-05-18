package at.wtioit.intellij.plugins.odoo.modules;

import at.wtioit.intellij.plugins.odoo.index.OdooIndexEntry;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public interface OdooModule extends OdooIndexEntry {

    @NotNull
    String getName();

    @Nullable
    PsiElement getDirectory();

    @NotNull
    String getPath();

    @Nullable
    Icon getIcon();

    @Nullable
    String getRelativeLocationString();

    @NotNull
    Collection<OdooModule> getDependencies();

    @NotNull
    List<OdooModel> getModels();

    boolean dependsOn(@NotNull OdooModule module);

    PsiFile getManifestFile();

    @Nullable
    NavigatablePsiElement getNavigationItem();
}
