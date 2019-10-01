package at.wtioit.intellij.plugins.odoo.models;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface OdooModel {

    Set<String> ODOO_MODEL_NAME_VARIABLE_NAME = new HashSet<>(Arrays.asList("_inherit", "_name"));
    Set<String> ODOO_MODEL_NAME_FIELD_NAMES = new HashSet<>(Arrays.asList("fields.Many2many", "fields.Many2one", "fields.One2many"));

    String getName();

    List<OdooModule> getModules();
}
