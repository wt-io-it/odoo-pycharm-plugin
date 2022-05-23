package at.wtioit.intellij.plugins.odoo.index;

import at.wtioit.intellij.plugins.odoo.AbstractDataExternalizer;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

public class OdooIndexKeysIndex extends OdooIndexExtension<OdooKeyIndexEntry> {

    public static final ID<String, OdooKeyIndexEntry> NAME = ID.create("OdooIndexKeysIndex");

    @Override
    public <E extends OdooIndexEntry> OdooKeyIndexEntry castValue(E entry) {
        if (entry instanceof OdooKeyIndexEntry) {
            return (OdooKeyIndexEntry) entry;
        }
        throw new OdooIndexError("expected entry to be of type OdooModelDefinition");
    }

    @Override
    public @NotNull ID<String, OdooKeyIndexEntry> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, OdooKeyIndexEntry, FileContent> getIndexer() {
        return null;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return null;
    }

    @Override
    public @NotNull DataExternalizer<OdooKeyIndexEntry> getValueExternalizer() {
        return new AbstractDataExternalizer<OdooKeyIndexEntry>() {
            @Override
            public void save(@NotNull DataOutput out, OdooKeyIndexEntry value) throws IOException {
                saveInteger(value.getKeys().size(), out);
                for (String key : value.getKeys()) {
                    saveString(key, out);
                }
            }


            @Override
            public OdooKeyIndexEntry read(@NotNull DataInput in) throws IOException {
                int size = readInteger(in);
                ArrayList<String> keys = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    keys.add(readString(in));
                }
                return new OdooKeyIndexEntry(keys);
            }
        };
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return (file -> Boolean.FALSE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return false;
    }
}
