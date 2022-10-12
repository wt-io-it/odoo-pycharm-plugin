package at.wtioit.intellij.plugins.odoo.models.search;

import at.wtioit.intellij.plugins.odoo.icons.OdooPluginIcons;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexSubKeys;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.search.AbstractOdooPsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.ui.LayeredIcon;
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
        return OdooPluginIcons.getOdooIconForPsiElement(model.getDefiningElement());
    }

    @Override
    public OdooIndexSubKeys getSubIndexKey() {
        return OdooIndexSubKeys.ODOO_MODELS;
    }
}
