package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.OdooBundle;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.compatibility.CompatibleFileIndex;
import at.wtioit.intellij.plugins.odoo.index.IndexWatcher;
import at.wtioit.intellij.plugins.odoo.index.OdooIndex;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexSubKeys;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.modules.index.OdooDeserializedModuleImpl;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OdooModuleServiceImpl implements OdooModuleService {

    private static final List<String> COMMON_ODOO_MODULE_SUBDIRS = Arrays.asList("data", "demo", "views", "models", "tests");
    Project project;

    public OdooModuleServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public Iterable<OdooModule> getModules() {
        List<OdooModule> modules = new ArrayList<>();
        OdooIndex.getAllKeyValuesPairs(project, OdooModule.class).forEach(nameModules -> {
            List<OdooModule> modulesForName = nameModules.getSecond();
            if (modulesForName.size() > 1) {
                showDuplicateModuleWarning(nameModules.getFirst());
            }
            modules.addAll(modulesForName);
        });
        return modules;
    }

    @Override
    public Stream<String> getModuleNames() {
        return OdooIndex.getAllKeys(OdooIndexSubKeys.ODOO_MODULES, project);
    }

    @Nullable
    @Override
    public OdooModule getModule(String moduleName) {
        if(!IndexWatcher.isCalledInIndexJob()) {
            return ApplicationManager.getApplication().runReadAction((Computable<OdooModule>) () -> {
                GlobalSearchScope scope = GlobalSearchScope.allScope(project);
                List<OdooModule> modulesForName = OdooIndex.getValues(moduleName, scope, OdooModule.class).collect(Collectors.toList());
                if (modulesForName.size() > 1) {
                    showDuplicateModuleWarning(moduleName);
                    return modulesForName.get(0);
                } else if (modulesForName.size() == 1) {
                    return modulesForName.get(0);
                }
                return null;
            });
        }
        return null;
    }

    @Nullable
    @Override
    public OdooModule getModule(@Nullable VirtualFile file) {
        if (file == null) return null;
        return ApplicationManager.getApplication().runReadAction((Computable<OdooModule>) () -> {
            PsiDirectory moduleDirectory = getModuleDirectory(file.getPath());
            if (moduleDirectory != null) {
                VirtualFile manifest = moduleDirectory.getVirtualFile().findFileByRelativePath("__manifest__.py");
                if (manifest != null) {
                    Map<String, OdooModule> modules = OdooIndex.getFileData(manifest, project, OdooModule.class);
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
        return WithinProject.call(project, () -> {
            for (VirtualFile file : CompatibleFileIndex.getVirtualFilesByName("odoo-bin", scope)) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                if (psiFile != null) {
                    return psiFile.getContainingDirectory();
                }
            }
            return null;
        });
    }

    @Override
    public PsiDirectory getModuleDirectory(String location) {
        // guess a module first (fast path)
        // windows locations have slashes here as well (we get them from getPath())
        String[] path = location.split("/");
        String moduleName = null;
        if (path.length >= 2 && "addons".equals(path[path.length - 2])) {
            moduleName = path[path.length - 1];
        } else if (path.length >= 3 && COMMON_ODOO_MODULE_SUBDIRS.contains(path[path.length - 2])) {
            moduleName = path[path.length - 3];
        }
        if (moduleName != null) {
            OdooModule module = getModule(moduleName);
            if (module != null) {
                if (module instanceof OdooDeserializedModuleImpl && location.equals(module.getPath())) {
                    // skip fast path for deserialized modules direct module path
                    // slow path, search all __manifest__.py files
                    return getModuleDirectorySlow(location);
                } else if (location.equals(module.getPath()) || location.startsWith(module.getPath() + File.separator)) {
                    return WithinProject.call(project, () -> (PsiDirectory) module.getDirectory());
                }
            }
        }

        // slow path, search all __manifest__.py files
        return getModuleDirectorySlow(location);
    }

    private PsiDirectory getModuleDirectorySlow(String location) {
        // Windows paths have slashes here as well (we get the from getPath())
        return ApplicationManager.getApplication().runReadAction((Computable<PsiDirectory>) () -> {
            // TODO iterate over known modules first, before accessing FilenameIndex
            // TODO do not acccess FilenameIndex while indexing
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            return WithinProject.call(project, () -> {
                for (VirtualFile file : CompatibleFileIndex.getVirtualFilesByName("__manifest__.py", scope)) {
                    VirtualFile directory = file.getParent();
                    if (directory != null) {
                        String directoryPath = directory.getPath();
                        if (OdooModuleService.isValidOdooModuleDirectory(directoryPath) && location.equals(directoryPath) || location.startsWith(directoryPath + "/")) {
                            return PsiManager.getInstance(project).findDirectory(directory);
                        }
                    }
                }
                return null;
            });
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

        public DuplicateModulesWarning(String moduleName) {
            super(OdooBundle.message("NOTIFICATION.GROUP.odoo"),
                    OdooBundle.message("NOTIFICATION.duplicate.module.title"),
                    OdooBundle.message("NOTIFICATION.duplicate.module.content", moduleName),
                    NotificationType.WARNING);
        }
    }
}
