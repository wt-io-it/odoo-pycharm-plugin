package at.wtioit.intellij.plugins.odoo.modules.index;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class OdooModuleFileIndex extends FileBasedIndexExtension<String, OdooModule> {

    @NonNls public static final ID<String, OdooModule> NAME = ID.create("OdooModuleFileIndex");

    OdooModuleFileIndexer indexer = new OdooModuleFileIndexer();

    @Override
    public @NotNull ID<String, OdooModule> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, OdooModule, FileContent> getIndexer() {
        return indexer;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<OdooModule> getValueExternalizer() {
        return new DataExternalizer<OdooModule>() {
            @Override
            public void save(@NotNull DataOutput out, OdooModule module) throws IOException {
                String name = module.getName();
                out.writeInt(name.length());
                out.writeBytes(name);
                String path = module.getPath();
                out.writeInt(path.length());
                out.writeBytes(path);
            }

            @Override
            public OdooModule read(@NotNull DataInput in) throws IOException {
                String name = readString(in);
                String path = readString(in);
                return new OdooDeserializedModuleImpl(name, path);
            }

            @NotNull
            private String readString(@NotNull DataInput in) throws IOException {
                int nameLength = in.readInt();
                byte[] nameBytes = new byte[nameLength];
                for (int i = 0; i < nameLength; i++) {
                    nameBytes[i] = in.readByte();
                }
                return new String(nameBytes);
            }
        };
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull VirtualFile file) {
                return "__manifest__.py".equals(file.getName());
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return false;
    }

    private static class OdooModuleFileIndexer implements DataIndexer<String, OdooModule, FileContent> {
        @Override
        public @NotNull Map<String, OdooModule> map(@NotNull FileContent inputData) {
            OdooVirtualFileModuleImpl module = new OdooVirtualFileModuleImpl(inputData.getFile());
            return Collections.singletonMap(module.getName(), module);
        }
    }
}
