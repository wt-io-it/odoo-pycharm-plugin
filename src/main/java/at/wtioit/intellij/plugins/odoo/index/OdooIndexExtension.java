package at.wtioit.intellij.plugins.odoo.index;

import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public abstract class OdooIndexExtension<T extends OdooIndexEntry> extends FileBasedIndexExtension<String, T> {

    public abstract <E extends OdooIndexEntry> T castValue(E entry);

    public void save(@NotNull DataOutput out, OdooIndexEntry value) throws IOException {
        getValueExternalizer().save(out, castValue(value));
    }

    public T read(@NotNull DataInput in) throws IOException {
        return getValueExternalizer().read(in);
    }

    public Map<String, OdooIndexEntry> map(FileContent inputData) {
        if (getInputFilter().acceptInput(inputData.getFile())) {
            return (Map<String, OdooIndexEntry>) getIndexer().map(inputData);
        }
        return Collections.emptyMap();
    }
}
