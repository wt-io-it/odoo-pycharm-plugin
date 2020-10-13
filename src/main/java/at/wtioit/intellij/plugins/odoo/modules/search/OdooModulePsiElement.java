package at.wtioit.intellij.plugins.odoo.modules.search;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
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
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class OdooModulePsiElement extends AbstractOdooPsiElement implements OdooModule {

    private final OdooModule module;

    public OdooModulePsiElement(OdooModule forModule, Project project) {
        super(project, forModule::getDirectory);
        module = forModule;
    }

    @Override
    public @NotNull String getName() {
        return module.getName();
    }

    @Override
    public String getLocationString() {
        return getRelativeLocationString();
    }

    @Override
    public @Nullable PsiElement getDirectory() {
        return module.getDirectory();
    }

    @Override
    public @NotNull String getPath() {
        return module.getPath();
    }

    @Override
    public @Nullable Icon getIcon() {
        return module.getIcon();
    }

    @Override
    public @Nullable String getRelativeLocationString() {
        return module.getRelativeLocationString();
    }

    @Override
    public @NotNull Collection<OdooModule> getDependencies() {
        return module.getDependencies();
    }

    @Override
    public @NotNull List<OdooModel> getModels() {
        return module.getModels();
    }

    @Override
    public boolean dependsOn(OdooModule module) {
        return module.dependsOn(module);
    }

    @Override
    public PsiFile getManifestFile() {
        return module.getManifestFile();
    }

    @Override
    public Icon getIcon(int flags) {
        return module.getIcon();
    }
}
