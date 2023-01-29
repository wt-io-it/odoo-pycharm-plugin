package at.wtioit.intellij.plugins.odoo.index;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
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
        if (DumbService.isDumb(project)) {
            // project is marked as fully indexed, but we are still in dumb mode
            // we return false to avoid IndexNotReadyExceptions
            return false;
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
            if (previousValue && projectNeedsCacheClear(project)) {
                clearCaches(project);
            }
        }
    }

    private static void clearPythonModuleCache(Project project) {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            PythonModulePathCache.getInstance(module).clearCache();
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

    public static void dumbModeLeft(Project project) {
        // when we are leaving dumb mode in case the UnindexedFilesUpdaterListener is not working (2022.3+)
        // we trigger the cache clear upon leaving dumb mode
        if (projectNeedsCacheClear(project)) {
            if (!projectIndexerRunning.getOrDefault(project, new AtomicBoolean(false)).get()) {
                clearCaches(project);
            }
        }
    }

    private static boolean projectNeedsCacheClear(Project project) {
        return projectNeedsCacheClearWhenFullyIndexed.getOrDefault(project, new AtomicBoolean(false)).get();
    }

    private static void clearCaches(Project project) {
        if (projectNeedsCacheClear(project)) {
            synchronized (projectNeedsCacheClearWhenFullyIndexed) {
                if (projectNeedsCacheClear(project)) {
                    // Clear PythonModulePathCache to get rid of cached null values for modules in PythonModulePathCache
                    clearPythonModuleCache(project);
                    // mark project as no longer needing cache clear
                    projectNeedsCacheClearWhenFullyIndexed.get(project).set(false);
                }
            }
        }
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
