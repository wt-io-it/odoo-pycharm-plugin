package at.wtioit.intellij.plugins.odoo.index;

import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface CrossSubIndexMapper {
    void mapIndexEntries(@NotNull Map<String, OdooIndexEntry> indexValues, @NotNull FileContent inputData);
}
