package at.wtioit.intellij.plugins.odoo.index;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.psi.resolve.PythonModulePathCache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class IndexWatcher extends ThreadLocal<IndexWatcher.IndexState> {
    public static IndexWatcher INSTANCE = new IndexWatcher();

    public final static Map<Project, AtomicBoolean> projectIndexerRunning = new HashMap<>();
    public final static Map<Project, AtomicBoolean> projectNeedsCacheClearWhenFullyIndexed = new HashMap<>();

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
        if (!project.isInitialized()) {
            return false;
        }
        if (projectIndexerRunning.containsKey(project)) {
            return !projectIndexerRunning.get(project).get();
        }
        return true;
    }

    public static void updateStarted(Project project) {
        if (!projectIndexerRunning.containsKey(project)) {
            synchronized (projectIndexerRunning) {
                if (!projectIndexerRunning.containsKey(project)) {
                    projectIndexerRunning.put(project, new AtomicBoolean(false));
                }
            }
        }
        projectIndexerRunning.get(project).set(true);
    }

    public static void updateFinished(Project project) {
        if (projectIndexerRunning.containsKey(project)) {
            boolean previousValue = projectIndexerRunning.get(project).getAndSet(false);

            if (previousValue && projectNeedsCacheClearWhenFullyIndexed.containsKey(project) && projectNeedsCacheClearWhenFullyIndexed.get(project).get()) {
                // Clear PythonModulePathCache to get rid of cached null values for modules in PythonModulePathCache
                for (Module module : ModuleManager.getInstance(project).getModules()) {
                    PythonModulePathCache.getInstance(module).clearCache();
                }
            }
        }
    }

    public static void needsCacheClearWhenFullyIndexed(Project project) {
        if (!projectNeedsCacheClearWhenFullyIndexed.containsKey(project)) {
            synchronized (projectNeedsCacheClearWhenFullyIndexed) {
                if (!projectNeedsCacheClearWhenFullyIndexed.containsKey(project)) {
                    projectNeedsCacheClearWhenFullyIndexed.put(project, new AtomicBoolean(false));
                }
            }
        }
        projectNeedsCacheClearWhenFullyIndexed.get(project).set(true);
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
