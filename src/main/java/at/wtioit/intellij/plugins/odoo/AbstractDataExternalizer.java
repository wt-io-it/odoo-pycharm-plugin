package at.wtioit.intellij.plugins.odoo;

import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class AbstractDataExternalizer<T> implements DataExternalizer<T> {

    private static final int NULL_VALUE_MARKER = Integer.MAX_VALUE;

    protected String readString(@NotNull DataInput in) throws IOException {
        int nameLength = in.readInt();
        if (nameLength != NULL_VALUE_MARKER) {
            byte[] nameBytes = new byte[nameLength];
            for (int i = 0; i < nameLength; i++) {
                nameBytes[i] = in.readByte();
            }
            return new String(nameBytes);
        } else {
            return null;
        }
    }

    protected void saveString(String value, @NotNull DataOutput out) throws IOException {
        if (value != null) {
            out.writeInt(value.length());
            out.writeBytes(value);
        } else {
            out.writeInt(NULL_VALUE_MARKER);
        }
    }
}
