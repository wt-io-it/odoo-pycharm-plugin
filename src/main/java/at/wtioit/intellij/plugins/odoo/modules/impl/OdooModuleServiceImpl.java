package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.modules.index.OdooModuleFileIndex;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OdooModuleServiceImpl implements OdooModuleService {

    Project project;

    public OdooModuleServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public Iterable<OdooModule> getModules() {
        List<OdooModule> modules = new ArrayList<>();
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
        return ApplicationManager.getApplication().runReadAction((Computable<OdooModule>) () -> {
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
        });
    }

    @Override
    public OdooModule getModule(PsiFile file) {
        return getModule(file.getVirtualFile());
    }

    @Override
    public OdooModule getModule(VirtualFile file) {
        return ApplicationManager.getApplication().runReadAction((Computable<OdooModule>) () -> {
            PsiDirectory moduleDirectory = getModuleDirectory(file.getPath());
            if (moduleDirectory != null) {
                FileBasedIndex index = FileBasedIndex.getInstance();
                VirtualFile manifest = moduleDirectory.getVirtualFile().findFileByRelativePath("__manifest__.py");
                if (manifest != null) {
                    Map<String, OdooModule> modules = index.getFileData(OdooModuleFileIndex.NAME, manifest, project);
                    if (modules.size() == 1) {
                        return modules.values().iterator().next();
                    }
                }
            }
            return null;
        });
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
        return ApplicationManager.getApplication().runReadAction((Computable<PsiDirectory>) () -> {
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            for (PsiFile file : FilenameIndex.getFilesByName(project, "__manifest__.py", scope)) {
                PsiDirectory directory = file.getParent();
                if (directory != null) {
                    String directoryPath = directory.getVirtualFile().getCanonicalPath();
                    if (location.equals(directoryPath) || location.startsWith(directoryPath + File.separator)) {
                        return directory;
                    }
                }
            }
            return null;
        });
    }
}
