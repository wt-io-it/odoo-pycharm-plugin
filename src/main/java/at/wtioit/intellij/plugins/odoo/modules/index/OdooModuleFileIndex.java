package at.wtioit.intellij.plugins.odoo.modules.index;

import at.wtioit.intellij.plugins.odoo.AbstractDataExternalizer;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
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
        return new AbstractDataExternalizer<OdooModule>() {
            @Override
            public void save(@NotNull DataOutput out, OdooModule module) throws IOException {
                saveString(module.getName(), out);
                saveString(module.getPath(), out);
            }

            @Override
            public OdooModule read(@NotNull DataInput in) throws IOException {
                String name = readString(in);
                String path = readString(in);
                return new OdooDeserializedModuleImpl(name, path);
            }
        };
    }

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        // * an odoo module has __manifest__.py as a definition file
        // * /setup/.. modules are just a copy of the ones not in /setup/
        return file -> "__manifest__.py".equals(file.getName()) && !file.getPath().contains(File.separator + "setup" + File.separator);
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
