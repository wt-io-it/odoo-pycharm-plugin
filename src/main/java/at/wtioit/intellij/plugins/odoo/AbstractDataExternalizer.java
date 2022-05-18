package at.wtioit.intellij.plugins.odoo;

import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;

public abstract class AbstractDataExternalizer<T> implements DataExternalizer<T> {

    private static final int NULL_VALUE_MARKER = Integer.MAX_VALUE;

    protected String readString(@NotNull DataInput in) throws IOException {
        int nameLength = in.readInt();
        if (nameLength < 0) {
            // TODO index is corrupt we need to reindex everything
            return null;
        } else if (nameLength != NULL_VALUE_MARKER) {
            byte[] nameBytes = new byte[nameLength];
            for (int i = 0; i < nameLength; i++) {
                nameBytes[i] = in.readByte();
            }
            return new String(nameBytes, Charset.defaultCharset());
        } else {
            return null;
        }
    }

    protected void saveString(String value, @NotNull DataOutput out) throws IOException {
        if (value != null) {
            byte[] bytes = value.getBytes(Charset.defaultCharset());
            out.writeInt(bytes.length);
            for (byte b : bytes) {
                out.writeByte(b);
            }
        } else {
            out.writeInt(NULL_VALUE_MARKER);
        }
    }

    protected int readInteger(@NotNull DataInput in) throws IOException {
        return in.readInt();
    }

    protected void saveInteger(int value, @NotNull DataOutput out) throws IOException {
        out.writeInt(value);
    }
}
