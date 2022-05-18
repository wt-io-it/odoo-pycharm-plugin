package at.wtioit.intellij.plugins.odoo.index;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OdooKeyIndexEntry implements OdooIndexEntry {

    private final List<String> keys;

    public OdooKeyIndexEntry(@NotNull List<String> keys) {
        this.keys = keys;
    }

    @Override
    public OdooIndexSubKeys getSubIndexKey() {
        return OdooIndexSubKeys.INDEX_KEYS;
    }

    public List<String> getKeys() {
        return keys;
    }

    @Override
    public int hashCode() {
        return keys.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooKeyIndexEntry that = (OdooKeyIndexEntry) o;
        return keys.equals(that.keys);
    }
}
