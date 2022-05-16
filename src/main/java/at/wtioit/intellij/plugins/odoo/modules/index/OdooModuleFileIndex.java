package at.wtioit.intellij.plugins.odoo.modules.index;

import at.wtioit.intellij.plugins.odoo.AbstractDataExternalizer;
import at.wtioit.intellij.plugins.odoo.index.OdooDataIndexer;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexEntry;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexError;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexExtension;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
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

public class OdooModuleFileIndex extends OdooIndexExtension<OdooModule> {

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
                return OdooDeserializedModuleImpl.getInstance(name, path);
            }
        };
    }

    @Override
    public int getVersion() {
        return 8;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        // an odoo module has __manifest__.py as a definition file
        return file -> "__manifest__.py".equals(file.getName())
                && OdooModuleService.isValidOdooModuleDirectory(file.getPath())
                // /posbox/overwrite_after_init/ may overwrite addons so we do not detect them as addons
                && !file.getPath().contains(File.separator + "posbox" + File.separator + "overwrite_after_init" + File.separator);
    }

    @Override
    public boolean dependsOnFileContent() {
        return false;
    }

    private static class OdooModuleFileIndexer extends OdooDataIndexer<OdooModule> {
        @Override
        public @NotNull Map<String, OdooModule> mapWatched(@NotNull FileContent inputData) {
            OdooVirtualFileModuleImpl module = new OdooVirtualFileModuleImpl(inputData.getFile());
            return Collections.singletonMap(module.getName(), module);
        }
    }

    @Override
    public <E extends OdooIndexEntry> OdooModule castValue(E entry) {
        if (entry instanceof OdooModule) {
            return (OdooModule) entry;
        }
        throw new OdooIndexError("expected entry to be of type OdooModule");
    }
}
