package at.wtioit.intellij.plugins.odoo.index;

import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class OdooDataIndexer<T> implements DataIndexer<String, T, FileContent> {
    public abstract @NotNull Map<String, T> mapWatched(@NotNull FileContent inputData);

    public @NotNull Map<String, T> map(@NotNull FileContent inputData) {
        return IndexWatcher.runIndexJob(() -> mapWatched(inputData));
    }
}
