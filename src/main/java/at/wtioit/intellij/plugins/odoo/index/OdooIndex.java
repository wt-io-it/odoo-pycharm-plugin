package at.wtioit.intellij.plugins.odoo.index;

import at.wtioit.intellij.plugins.odoo.AbstractDataExternalizer;
import at.wtioit.intellij.plugins.odoo.models.index.OdooModelFileIndex;
import at.wtioit.intellij.plugins.odoo.modules.index.OdooModuleFileIndex;
import at.wtioit.intellij.plugins.odoo.records.index.OdooRecordFileIndex;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OdooIndex extends FileBasedIndexExtension<String, OdooIndexEntry> {

    @NonNls
    public static final ID<String, OdooIndexEntry> NAME = ID.create("OdooIndex");

    OdooIndexer indexer = new OdooIndexer();

    protected static final Map<String, OdooIndexExtension<? extends OdooIndexEntry>> subIndexExtensions = initSubIndexExtensions();

    private static Map<String, OdooIndexExtension<? extends OdooIndexEntry>> initSubIndexExtensions() {
        HashMap<String, OdooIndexExtension<? extends OdooIndexEntry>> map = new HashMap<>();
        map.put(OdooIndexSubKeys.ODOO_RECORDS.name(), new OdooRecordFileIndex());
        map.put(OdooIndexSubKeys.ODOO_MODELS.name(), new OdooModelFileIndex());
        map.put(OdooIndexSubKeys.ODOO_MODULES.name(), new OdooModuleFileIndex());
        map.put(OdooIndexSubKeys.INDEX_KEYS.name(), new OdooIndexKeysIndex());
        return map;
    }

    // TODO unify the arguments for those methods

    public static <T extends OdooIndexEntry> Stream<T> getValues(String key, GlobalSearchScope scope, Class<T> clazz) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        List<OdooIndexEntry> values = index.getValues(OdooIndex.NAME, key, scope);
        return values.stream()
                .filter(v -> clazz.isAssignableFrom(v.getClass()))
                .map(clazz::cast);
    }

    public static Stream<String> getAllKeys(OdooIndexSubKeys odooIndexSubKeys, Project project) {
        // TODO this seems very slow (could use an own index ;-) )
        FileBasedIndex index = FileBasedIndex.getInstance();
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        // TODO if we add all keys under a second key (v.getSubIndexKey) as value
        // we can fetch all keys for a sub index very fast (i guess)
        if (true) {
            return index.getValues(OdooIndex.NAME, odooIndexSubKeys.name(), scope).stream()
                    .filter(e -> e instanceof OdooKeyIndexEntry)
                    .map(e -> (OdooKeyIndexEntry) e)
                    .flatMap(e -> e.getKeys().stream())
                    .distinct();
        }
        return index.getAllKeys(OdooIndex.NAME, project).stream()
                .filter(k -> index.getValues(OdooIndex.NAME, k, scope).stream().anyMatch(v -> v.getSubIndexKey() == odooIndexSubKeys));
    }

    public static <T extends OdooIndexEntry> Stream<Pair<String, List<T>>> getAllKeyValuesPairs(Project project, Class<T> clazz) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        return index.getAllKeys(OdooIndex.NAME, project).stream()
                .map(k -> Pair.create(k, index.getValues(OdooIndex.NAME, k, scope).stream()
                        .filter(v -> clazz.isAssignableFrom(v.getClass()))
                        .map(clazz::cast)
                        .collect(Collectors.toList())
                ));
    }

    public static <T extends OdooIndexEntry> Map<String, T> getFileData(VirtualFile file, Project project, Class<T> clazz) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        return index.getFileData(OdooIndex.NAME, file, project).entrySet().stream()
                .filter(e -> clazz.isAssignableFrom(e.getValue().getClass()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> clazz.cast(e.getValue())));
    }

    @Override
    public @NotNull ID<String, OdooIndexEntry> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, OdooIndexEntry, FileContent> getIndexer() {
        return indexer;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<OdooIndexEntry> getValueExternalizer() {
        return new AbstractDataExternalizer<OdooIndexEntry>() {

            @Override
            public void save(@NotNull DataOutput out, OdooIndexEntry value) throws IOException {
                String subIndexKey = value.getSubIndexKey().name();
                saveString(subIndexKey, out);
                subIndexExtensions.get(subIndexKey).save(out, value);
            }

            @Override
            public OdooIndexEntry read(@NotNull DataInput in) throws IOException {
                String subIndexKey = readString(in);
                return subIndexExtensions.get(subIndexKey).read(in);
            }
        };
    }

    @Override
    public int getVersion() {
        return 10;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return (file -> subIndexExtensions.values().stream()
                .map(FileBasedIndexExtension::getInputFilter)
                .anyMatch(inputFilter -> inputFilter.acceptInput(file)));
    }

    @Override
    public boolean dependsOnFileContent() {
        return subIndexExtensions.values().stream().anyMatch(FileBasedIndexExtension::dependsOnFileContent);
    }
}
