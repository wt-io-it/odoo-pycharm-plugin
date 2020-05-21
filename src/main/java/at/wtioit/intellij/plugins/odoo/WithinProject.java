package at.wtioit.intellij.plugins.odoo;

import com.intellij.openapi.project.Project;

import java.util.function.Supplier;

// TODO get rid of this hack once everything is migrated to indexes
public class WithinProject extends ThreadLocal<Project> {
    public static WithinProject INSTANCE = new WithinProject();

    public static <V> V call(Project project, Supplier<V> supplier) {
        if (INSTANCE.get() != null) return supplier.get();
        try {
            INSTANCE.set(project);
            return supplier.get();
        } finally {
            INSTANCE.remove();
        }
    }
}
