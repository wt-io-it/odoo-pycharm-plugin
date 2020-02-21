package at.wtioit.intellij.plugins.odoo;

import com.intellij.openapi.project.Project;

// TODO get rid of this hack once everything is migrated to indexes
public class WithinProject extends ThreadLocal<Project> {
    public static WithinProject INSTANCE = new WithinProject();
}
