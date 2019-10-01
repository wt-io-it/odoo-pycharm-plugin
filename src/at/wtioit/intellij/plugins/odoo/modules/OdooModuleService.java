package at.wtioit.intellij.plugins.odoo.modules;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface OdooModuleService {
    static OdooModuleService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, OdooModuleService.class);
    }

    Iterable<OdooModule> getModules();

    OdooModule getModule(String moduleName);
}
