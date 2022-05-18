package at.wtioit.intellij.plugins.odoo.index;

import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OdooIndexer implements DataIndexer<String, OdooIndexEntry, FileContent> {

    public OdooIndexer() {
    }

    @Override
    public @NotNull Map<String, OdooIndexEntry> map(@NotNull FileContent inputData) {
        Map<String, OdooIndexEntry> indexValues = OdooIndex.subIndexExtensions.values().stream()
                .map(odooIndexExtension -> odooIndexExtension.map(inputData))
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (indexValues.size() > 0) {
            for (Map.Entry<OdooIndexSubKeys, List<String>> entry : indexValues.entrySet().stream().collect(Collectors.groupingBy(
                    e -> e.getValue().getSubIndexKey(),
                    Collectors.mapping(e -> e.getKey(), Collectors.toList())
            )).entrySet()) {
                indexValues.put(entry.getKey().name(), new OdooKeyIndexEntry(entry.getValue()));
            }
        }
        return indexValues;
    }
}
