package at.wtioit.intellij.plugins.odoo.models.search;

import at.wtioit.intellij.plugins.odoo.icons.OdooPluginIcons;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.search.AbstractOdooPsiElement;
import at.wtioit.intellij.plugins.odoo.search.OdooSEResult;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;

public class OdooModelPsiElement extends AbstractOdooPsiElement implements OdooModel {

    private final OdooModel model;
    private Icon icon;

    public OdooModelPsiElement(OdooModel forModel, Project forProject) {
        super(forProject, forModel::getDefiningElement);
        model = forModel;
    }

    @Override
    public @Nullable String getName() {
        return model.getName();
    }

    @Override
    public String getLocationString() {
        ItemPresentation presentation = getContainingFile().getPresentation();
        if (presentation != null) {
            PsiElement definingElement = getDefiningElement();
            String locationString = presentation.getLocationString();
            if (definingElement instanceof PyClass && locationString != null) {
                return locationString.replace("(", "(" + ((PyClass) definingElement).getName() + " in ");
            }
            return locationString;
        }
        return null;
    }

    @Override
    public Set<OdooModule> getModules() {
        return model.getModules();
    }

    @NotNull
    @Override
    public PsiElement getDefiningElement() {
        return model.getDefiningElement();
    }

    @Override
    public OdooModule getBaseModule() {
        return model.getBaseModule();
    }

    @Override
    public Icon getIcon(int flags) {
        if (icon == null) {
            Icon baseIcon = model.getDefiningElement().getIcon(flags);
            icon = new LayeredIcon(baseIcon, OdooPluginIcons.ODOO_OVERLAY_ICON);
        }
        return icon;
    }
}
