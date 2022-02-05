package at.wtioit.intellij.plugins.odoo.index;

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
