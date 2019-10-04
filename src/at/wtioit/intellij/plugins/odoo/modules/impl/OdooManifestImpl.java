package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.modules.OdooManifest;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class OdooManifestImpl implements OdooManifest {

    private Collection<String> dependencyNames;
    private Collection<OdooModule> dependencies;
    private final Project project;


    OdooManifestImpl(Collection<String> dependencyNames, Project project){
        this.dependencyNames = dependencyNames;
        this.project = project;
    }

    @Override
    public Collection<OdooModule> getDependencies() {
        if (dependencyNames == null) return null;
        if (dependencies == null || dependencyNames.size() != dependencies.size()) {
            OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
            ArrayList<OdooModule> dependencyModules = new ArrayList<>();
            for (String dependencyName : dependencyNames) {
                OdooModule module = moduleService.getModule(dependencyName);
                if (module != null) {
                    dependencyModules.add(module);
                } else {
                    // TODO missing dependency?
                    dependencyModules.add(new ResolveLaterOdooModuleImpl(dependencyName, project));
                }
            }
            dependencies = Collections.unmodifiableCollection(dependencyModules);
        }
        return dependencies;
    }

}
