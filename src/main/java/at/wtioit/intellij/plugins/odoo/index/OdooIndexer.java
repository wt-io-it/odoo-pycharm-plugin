package at.wtioit.intellij.plugins.odoo.index;

import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

public class OdooIndexer implements DataIndexer<String, OdooIndexEntry, FileContent> {

    public OdooIndexer() {
    }

    @Override
    public @NotNull Map<String, OdooIndexEntry> map(@NotNull FileContent inputData) {
        return OdooIndex.subIndexExtensions.values().stream()
                .map(odooIndexExtension -> odooIndexExtension.map(inputData))
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
