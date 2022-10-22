package at.wtioit.intellij.plugins.odoo.index;

import at.wtioit.intellij.plugins.odoo.models.index.OdooModelIE;
import at.wtioit.intellij.plugins.odoo.records.index.OdooModelDefinitionRecord;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds an OdooRecord index entry for every OdooModel it finds
 */
public class OdooModelRecordSubIndexMapper implements CrossSubIndexMapper {
    @Override
    public void mapIndexEntries(@NotNull Map<String, OdooIndexEntry> indexValues, @NotNull FileContent inputData) {
        HashMap<String, OdooIndexEntry> mappedValues = new HashMap<>();
        for (OdooIndexEntry entry : indexValues.values()) {
            if (entry.getSubIndexKey() == OdooIndexSubKeys.ODOO_MODELS && entry instanceof OdooModelIE) {
                OdooModelDefinitionRecord record = new OdooModelDefinitionRecord((OdooModelIE) entry, inputData.getFile().getPath());
                mappedValues.put(record.getXmlId(), record);
            }
        }
        indexValues.putAll(mappedValues);
    }
}
