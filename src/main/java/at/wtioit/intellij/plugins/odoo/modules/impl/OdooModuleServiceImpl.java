package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;

import java.util.*;

public class OdooModuleServiceImpl implements OdooModuleService {

    Project project;

    public OdooModuleServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public Iterable<OdooModule> getModules() {
        List<OdooModule> modules = new ArrayList<OdooModule>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        for (String moduleName : index.getAllKeys(OdooModuleFileIndex.NAME, project)) {
            List<OdooModule> modulesForName = index.getValues(OdooModuleFileIndex.NAME, moduleName, scope);
            if (modulesForName.size() > 1) {
                // TODO log an error to event log (or mark the directories as invalid)
                throw new IllegalStateException("More than one module for name " + moduleName);
            }
            modules.addAll(modulesForName);
        }
        return modules;
    }

    @Override
    public OdooModule getModule(String moduleName) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        List<OdooModule> modulesForName = index.getValues(OdooModuleFileIndex.NAME, moduleName, scope);
        if (modulesForName.size() > 1) {
            // TODO log an error to event log (or mark the directories as invalid)
            throw new IllegalStateException("More than one module for name " + moduleName);
        } else if (modulesForName.size() == 1) {
            return modulesForName.get(0);
        }
        return null;
    }

    @Override
    public OdooModule findModule(String moduleName) {
        return getModule(moduleName);
    }

    @Override
    public PsiDirectory getOdooDirectory() {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        for (PsiFile file : FilenameIndex.getFilesByName(project, "odoo-bin", scope)) {
            return file.getContainingDirectory();
        }
        return null;
    }

    @Override
    public PsiDirectory getModuleDirectory(String location) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        for (PsiFile file : FilenameIndex.getFilesByName(project, "__manifest__.py", scope)) {
            if (location.equals(file.getParent().getVirtualFile().getCanonicalPath())) {
                return file.getParent();
            }
        }
        return null;
    }
}
