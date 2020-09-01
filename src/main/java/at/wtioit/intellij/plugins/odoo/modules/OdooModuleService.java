package at.wtioit.intellij.plugins.odoo.modules;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public interface OdooModuleService {
    static OdooModuleService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, OdooModuleService.class);
    }

    Iterable<OdooModule> getModules();

    OdooModule getModule(String moduleName);

    OdooModule getModule(PsiFile file);

    OdooModule getModule(VirtualFile file);

    OdooModule findModule(String moduleName);

    PsiDirectory getOdooDirectory();

    PsiDirectory getModuleDirectory(String path);
}
