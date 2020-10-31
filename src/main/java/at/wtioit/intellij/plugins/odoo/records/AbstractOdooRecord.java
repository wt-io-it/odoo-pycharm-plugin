package at.wtioit.intellij.plugins.odoo.records;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractOdooRecord implements OdooRecord {


    protected final String modelName;
    private final String path;
    private final String id;
    @Nullable
    private final String xmlId;

    protected AbstractOdooRecord(@NotNull String id, @Nullable String xmlId, @NotNull String modelName, @NotNull String path) {
        this.id = id;
        this.xmlId = xmlId;
        this.modelName = modelName;
        this.path = path;
    }

    @NotNull
    @Override
    public String getModelName() {
        return modelName;
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    @Nullable
    @Override
    public String getXmlId() {
        if (xmlId == null) {
            if (WithinProject.INSTANCE.get() != null) {
                OdooModuleService moduleService = ServiceManager.getService(WithinProject.INSTANCE.get(), OdooModuleService.class);
                PsiDirectory moduleDirectory = moduleService.getModuleDirectory(path);
                if (moduleDirectory != null) {
                    // TODO update index
                    return moduleDirectory.getName() + "." + getId();
                } else {
                    // TODO remove from index?
                    return null;
                }
            } else {
                // TODO remove from index?
                return null;
            }
        }
        return xmlId;
    }

    @NotNull
    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AbstractOdooRecord) {
            return Objects.equals(modelName, ((AbstractOdooRecord) o).modelName)
                    && Objects.equals(id, ((AbstractOdooRecord) o).id)
                    && Objects.equals(xmlId, ((AbstractOdooRecord) o).xmlId)
                    && Objects.equals(path, ((AbstractOdooRecord) o).path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + ((xmlId != null) ? xmlId.hashCode() : 0);
        result = 31 * result + modelName.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }
}