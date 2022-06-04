package at.wtioit.intellij.plugins.odoo;

import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class AbstractDataExternalizer<T> implements DataExternalizer<T> {

    private static final int NULL_VALUE_MARKER = Integer.MAX_VALUE;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    protected String readString(@NotNull DataInput in) throws IOException {
        int nameLength = in.readInt();
        if (nameLength != NULL_VALUE_MARKER) {
            byte[] nameBytes = new byte[nameLength];
            for (int i = 0; i < nameLength; i++) {
                nameBytes[i] = in.readByte();
            }
            return new String(nameBytes, CHARSET);
        } else {
            return null;
        }
    }

    protected void saveString(String value, @NotNull DataOutput out) throws IOException {
        if (value != null) {
            byte[] bytes = value.getBytes(CHARSET);
            out.writeInt(bytes.length);
            out.write(bytes);
        } else {
            out.writeInt(NULL_VALUE_MARKER);
        }
    }
}
