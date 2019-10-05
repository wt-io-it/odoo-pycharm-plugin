package at.wtioit.intellij.plugins.odoo.models;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface OdooModel {

    Set<String> ODOO_MODEL_NAME_VARIABLE_NAME = new HashSet<>(Arrays.asList("_inherit", "_name"));
    Set<String> ODOO_MODEL_NAME_FIELD_NAMES = new HashSet<>(Arrays.asList("fields.Many2many", "fields.Many2one", "fields.One2many"));
    Set<String> ODOO_MODEL_NAME_FIELD_KEYWORD_ARGUMENTS = new HashSet<>(Collections.singletonList("comodel_name"));

    @Nullable
    String getName();

    List<OdooModule> getModules();

    void setModules(List<OdooModule> modelModules);

    PsiElement getDefiningElement();

}
