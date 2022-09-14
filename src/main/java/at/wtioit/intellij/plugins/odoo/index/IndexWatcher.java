package at.wtioit.intellij.plugins.odoo.index;

import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.UnindexedFilesUpdater;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class IndexWatcher extends ThreadLocal<IndexWatcher.IndexState> {
    public static IndexWatcher INSTANCE = new IndexWatcher();

    public static <T> T runIndexJob(Supplier<T> supplier) {
        try {
            if (INSTANCE.get() == null) {
                INSTANCE.set(new IndexState());
            }
            INSTANCE.get().insideIndex = true;
            return supplier.get();
        } finally {
            INSTANCE.get().insideIndex = false;
        }
    }

    public static boolean isFullyIndexed(Project project) {
        boolean fullyScanned = true;
        try {
            Method isProjectContentFullyScanned = UnindexedFilesUpdater.class.getDeclaredMethod("isProjectContentFullyScanned", Project.class);
            fullyScanned = (boolean) isProjectContentFullyScanned.invoke(UnindexedFilesUpdater.class, project);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            // we ignore exceptions here because isProjectContentFullyScanned is marked for deletion with 2023.1
            // we still need it as an indicator, because when switching projects we might be called right aways before
            // the "official" isIndexUpdateInProgress is returning true
        }
        if (!fullyScanned) {
            return false;
        }
        if (UnindexedFilesUpdater.isIndexUpdateInProgress(project)) {
            return false;
        }
        return true;
    }

    static class IndexState {
        boolean insideIndex = false;
    }

    public static boolean isCalledInIndexJob() {
        IndexState state = INSTANCE.get();
        if (state != null) {
            return state.insideIndex;
        } else {
            return false;
        }
    }
}
