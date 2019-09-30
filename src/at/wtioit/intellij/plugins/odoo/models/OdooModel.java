package at.wtioit.intellij.plugins.odoo.models;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface OdooModel {

    public static final Set<String> ODOO_MODEL_NAME_VARIABLE_NAME = new HashSet<>(Arrays.asList("_inherit", "_name"));

    String getName();

    List<OdooModule> getModules();
}
