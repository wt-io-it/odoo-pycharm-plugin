package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.*;

public class OdooModuleServiceImpl implements OdooModuleService {

    // TODO use a set?
    Collection<OdooModule> moduleCache;
    Map<String, OdooModule> moduleCacheByName;
    Project project;

    public OdooModuleServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public Iterable<OdooModule> getModules() {
        // TODO clear the cache?
        if (moduleCache == null) {
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            List<OdooModule> modules = new ArrayList<OdooModule>();
            for (PsiFile file : FilenameIndex.getFilesByName(project, "__manifest__.py", scope)) {
                PsiDirectory moduleDir = file.getParent();
                if (moduleDir != null
                        // TODO probably this exclude should be done via scope
                        && !moduleDir.toString().contains("/remote_sources/")) {
                    OdooModuleImpl module = new OdooModuleImpl(moduleDir, file);
                    modules.add(module);
                }
            }
            moduleCache = modules;
            HashMap<String, OdooModule> modulesByName = new HashMap<>();
            for (OdooModule module : modules) {
                modulesByName.put(module.getName(), module);
            }
            moduleCacheByName = modulesByName;
        }

        if (moduleCache == null) {
            return Collections.emptyList();
        } else {
            return moduleCache;
        }
    }

    @Override
    public OdooModule getModule(String moduleName) {
        if (moduleCacheByName == null) {
            getModules();
        }
        if (moduleCacheByName.containsKey(moduleName)) {
            return moduleCacheByName.get(moduleName);
        }
        return null;
    }

    @Override
    public OdooModule findModule(String moduleName) {
        OdooModule module = getModule(moduleName);
        if (module != null) {
            return module;
        } else {
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            for (PsiFile file : FilenameIndex.getFilesByName(project, "__manifest__.py", scope)) {
                PsiDirectory moduleDir = file.getParent();
                if (moduleDir != null
                        && moduleName.equals(moduleDir.getName())
                        // TODO probably this exclude should be done via scope
                        && !moduleDir.toString().contains("/remote_sources/")) {
                    module = new OdooModuleImpl(moduleDir, file);
                    if (moduleCache != null) {
                        moduleCache.add(module);
                        moduleCacheByName.put(moduleName, module);
                    }
                    return module;
                }
            }
        }
        return null;
    }

    @Override
    public PsiDirectory getOdooDirectory() {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        for (PsiFile file : FilenameIndex.getFilesByName(project, "odoo-bin", scope)) {
            return file.getContainingDirectory();
        }
        return null;
    }
}
