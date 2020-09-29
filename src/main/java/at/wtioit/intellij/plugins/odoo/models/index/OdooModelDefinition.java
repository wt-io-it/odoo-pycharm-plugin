package at.wtioit.intellij.plugins.odoo.models.index;

import at.wtioit.intellij.plugins.odoo.models.OdooModelUtil;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.psi.PyClass;

import java.util.Objects;

public class OdooModelDefinition {

    private final String className;
    private final String name;
    private final String project;
    private final String fileName;

    public OdooModelDefinition(PyClass pyClass) {
        name = OdooModelUtil.detectName(pyClass);
        project = pyClass.getProject().getPresentableUrl();
        fileName = pyClass.getContainingFile().getName();
        className = pyClass.getName();
    }

    public OdooModelDefinition(String fileName, String className, String modelName, Project project) {
        this.fileName = fileName;
        this.className = className;
        name = modelName;
        this.project = project.getPresentableUrl();
    }

    public OdooModelDefinition(String fileName, String className, String modelName, String projectPresentableUrl) {
        this.fileName = fileName;
        this.className = className;
        name = modelName;
        this.project = projectPresentableUrl;
    }

    public String getClassName() {
        return className;
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    public String getProject() {
        return project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OdooModelDefinition that = (OdooModelDefinition) o;

        if (!Objects.equals(className, that.className)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(project, that.project)) return false;
        return Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }
}
