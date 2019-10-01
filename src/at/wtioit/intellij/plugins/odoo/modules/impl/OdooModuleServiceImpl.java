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
    List<OdooModule> moduleCache;
    Project project;

    public OdooModuleServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public List<OdooModule> getModules() {
        // TODO clear the cache?
        if (moduleCache == null) {
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            List<OdooModule> modules = new ArrayList<OdooModule>();
            for (PsiFile file : FilenameIndex.getFilesByName(project, "__manifest__.py", scope)) {
                PsiDirectory moduleDir = file.getParent();
                if (moduleDir != null
                        && !"base".equals(moduleDir.getName())
                        // TODO probably this exclude should be done via scope
                        && !moduleDir.toString().contains("/remote_sources/")) {
                    OdooModuleImpl module = new OdooModuleImpl(moduleDir);
                    modules.add(module);
                }
            }
            moduleCache = modules;
        }

        if (moduleCache == null) {
            return Collections.emptyList();
        } else {
            return moduleCache;
        }
    }
}
