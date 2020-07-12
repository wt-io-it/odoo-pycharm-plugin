package at.wtioit.intellij.plugins.odoo;

import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class AbstractDataExternalizer<T> implements DataExternalizer<T> {
    @NotNull
    protected String readString(@NotNull DataInput in) throws IOException {
        int nameLength = in.readInt();
        byte[] nameBytes = new byte[nameLength];
        for (int i = 0; i < nameLength; i++) {
            nameBytes[i] = in.readByte();
        }
        return new String(nameBytes);
    }

    protected void saveString(String value, DataOutput out) throws IOException {
        out.writeInt(value.length());
        out.writeBytes(value);
    }
}
