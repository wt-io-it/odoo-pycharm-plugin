package at.wtioit.intellij.plugins.odoo.models.index;

import at.wtioit.intellij.plugins.odoo.models.OdooModelUtil;
import com.jetbrains.python.psi.PyClass;

public class OdooModelDefinition {

    private String className;
    private String name;
    private String project;
    private String fileName;

    public OdooModelDefinition(PyClass pyClass) {
        name = OdooModelUtil.detectName(pyClass);
        project = pyClass.getProject().getPresentableUrl();
        fileName = pyClass.getContainingFile().getName();
        className = pyClass.getName();
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
}
