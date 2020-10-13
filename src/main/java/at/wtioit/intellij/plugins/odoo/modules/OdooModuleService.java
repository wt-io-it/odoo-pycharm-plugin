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

    @Nullable
    OdooModule getModule(String moduleName);

    @Nullable
    OdooModule getModule(@Nullable VirtualFile file);

    OdooModule findModule(String moduleName);

    @Nullable
    PsiDirectory getOdooDirectory();

    @Nullable
    PsiDirectory getModuleDirectory(String path);

    static boolean isValidOdooModuleDirectory(@Nullable String path) {
        return path != null
                // /setup/.. modules are just a copy/symlink of the ones not in /setup/ (OCA)
                && !path.contains(File.separator + "setup" + File.separator)
                // when using remote debugging (e.g. with docker) pycharm may have remote sources that duplicate our modules
                && !path.contains(File.separator + "remote_sources" + File.separator);
    }
}
