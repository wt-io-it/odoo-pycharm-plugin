package at.wtioit.intellij.plugins.odoo.models.index;

import at.wtioit.intellij.plugins.odoo.index.OdooIndexEntry;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexSubKeys;
import at.wtioit.intellij.plugins.odoo.models.OdooModelUtil;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.psi.PyClass;

import java.util.Objects;

public class OdooModelIE implements OdooIndexEntry {

    private final String className;
    private final String name;
    private final String project;
    private final String fileName;

    public OdooModelIE(PyClass pyClass) {
        this(pyClass.getContainingFile().getName(), pyClass.getName(), OdooModelUtil.detectName(pyClass), pyClass.getProject().getPresentableUrl());
    }

    public OdooModelIE(String fileName, String className, String modelName, Project project) {
        this(fileName, className, modelName, project.getPresentableUrl());
    }

    public OdooModelIE(String fileName, String className, String modelName, String projectPresentableUrl) {
        this.fileName = fileName;
        this.className = className;
        name = modelName;
        this.project = projectPresentableUrl;
    }

    @Override
    public OdooIndexSubKeys getSubIndexKey() {
        return OdooIndexSubKeys.ODOO_MODELS;
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

        OdooModelIE that = (OdooModelIE) o;

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
