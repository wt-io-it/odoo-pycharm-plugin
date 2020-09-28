package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.modules.index.OdooDeserializedModuleImpl;
import at.wtioit.intellij.plugins.odoo.modules.index.OdooModuleFileIndex;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

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
                showDuplicateModuleWarning(moduleName);
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
                showDuplicateModuleWarning(moduleName);
                return modulesForName.get(0);
            } else if (modulesForName.size() == 1) {
                return modulesForName.get(0);
            }
            return null;
        });
    }

    @Nullable
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
        // guess a module first (fast path)
        String[] path = location.split(File.separator);
        String moduleName = null;
        if ("addons".equals(path[path.length - 2])) {
            moduleName = path[path.length - 1];
        } else if ("models".equals(path[path.length - 2])) {
            moduleName = path[path.length - 3];
        }
        if (moduleName != null) {
            OdooModule module = getModule(moduleName);
            if (module instanceof OdooDeserializedModuleImpl && location.equals(module.getPath())) {
                // skip fast path for deserialized modules direct module path
                // slow path, search all __manifest__.py files
                return getModuleDirectorySlow(location);
            } else if (location.equals(module.getPath()) || location.startsWith(module.getPath() + File.separator)) {
                return WithinProject.call(project, ()  -> (PsiDirectory) module.getDirectory());
            }
        }

        // slow path, search all __manifest__.py files
        return getModuleDirectorySlow(location);
    }

    private PsiDirectory getModuleDirectorySlow(String location) {
        return ApplicationManager.getApplication().runReadAction((Computable<PsiDirectory>) () -> {
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            for (PsiFile file : FilenameIndex.getFilesByName(project, "__manifest__.py", scope)) {
                PsiDirectory directory = file.getParent();
                if (directory != null) {
                    String directoryPath = directory.getVirtualFile().getPath();
                    if (OdooModuleService.isValidOdooModuleDirectory(directoryPath) && location.equals(directoryPath) || location.startsWith(directoryPath + File.separator)) {
                        return directory;
                    }
                }
            }
            return null;
        });
    }

    static final Set<String> warningsForModules = new HashSet<>();

    private void showDuplicateModuleWarning(String moduleName) {
        if (!warningsForModules.contains(moduleName)) {
            warningsForModules.add(moduleName);
            Notifications.Bus.notify(new DuplicateModulesWarning(moduleName), project);
        }
    }

    private class DuplicateModulesWarning extends Notification {

        static final String GROUP_DISPLAY_ID = "Odoo Module Notifications";

        public DuplicateModulesWarning(@NotNull String title, @NotNull String content, @NotNull NotificationType type) {
            super(GROUP_DISPLAY_ID, title, content, type);
        }

        public DuplicateModulesWarning(String moduleName) {
            super(GROUP_DISPLAY_ID,"Duplicate Module", "multiple modules with name " + moduleName + " detected.", NotificationType.WARNING);
        }
    }
}
