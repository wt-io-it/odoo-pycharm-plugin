package at.wtioit.intellij.plugins.odoo.modules;

import java.util.Collection;

public interface OdooManifest {
    Collection<OdooModule> getDependencies();
}
