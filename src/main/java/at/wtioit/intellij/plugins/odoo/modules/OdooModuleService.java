package at.wtioit.intellij.plugins.odoo.modules;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface OdooModuleService {
    static OdooModuleService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, OdooModuleService.class);
    }

    Iterable<OdooModule> getModules();

    OdooModule getModule(String moduleName);

    @Nullable
    OdooModule getModule(VirtualFile file);

    OdooModule findModule(String moduleName);

    @Nullable
    PsiDirectory getOdooDirectory();

    @Nullable
    PsiDirectory getModuleDirectory(String path);

    static boolean isValidOdooModuleDirectory(@Nullable String path) {
        // /setup/.. modules are just a copy/symlink of the ones not in /setup/
        return path != null && !path.contains(File.separator + "setup" + File.separator);
    }
}
