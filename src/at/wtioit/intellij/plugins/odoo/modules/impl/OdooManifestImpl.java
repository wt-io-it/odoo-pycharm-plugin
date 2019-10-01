package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.modules.OdooManifest;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;

import java.util.Collection;

public class OdooManifestImpl implements OdooManifest {

    private Collection<OdooModule> dependencies;

    public OdooManifestImpl(Collection<OdooModule> dependencies){
        this.dependencies = dependencies;
    }

    @Override
    public Collection<OdooModule> getDependencies() {
        return dependencies;
    }

}
