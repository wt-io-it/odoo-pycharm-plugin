package at.wtioit.intellij.plugins.odoo.records.index;

import at.wtioit.intellij.plugins.odoo.AbstractDataExternalizer;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
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
import java.util.Objects;

import static at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil.getRecordsFromCsvFile;
import static at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil.getRecordsFromFile;

public class OdooRecordFileIndex extends FileBasedIndexExtension<String, OdooRecord> {

    @NonNls public static final ID<String, OdooRecord> NAME = ID.create("OdooRecordFileIndex");

    private static final Logger logger = Logger.getInstance(OdooRecordFileIndex.class);

    OdooRecordFileIndexer indexer = new OdooRecordFileIndexer();

    @Override
    public @NotNull ID<String, OdooRecord> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, OdooRecord, FileContent> getIndexer() {
        return indexer;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<OdooRecord> getValueExternalizer() {
        return new AbstractDataExternalizer<OdooRecord>() {
            @Override
            public void save(@NotNull DataOutput out, OdooRecord record) throws IOException {
                saveString(record.getXmlId(), out);
                saveString(record.getPath(), out);
                saveString(record.getModelName(), out);
                if (Objects.equals(record.getId(), record.getXmlId())){
                    saveString(null, out);
                } else {
                    saveString(record.getId(), out);
                }
            }

            @Override
            public OdooRecord read(@NotNull DataInput in) throws IOException {
                String xmlId = readString(in);
                String path = readString(in);
                String modelName = readString(in);
                String id = readString(in);
                if (id == null && xmlId != null) {
                    return new OdooDeserializedRecordImpl(xmlId, xmlId, path, modelName);
                }
                return new OdooDeserializedRecordImpl(id, xmlId, path, modelName);
            }
        };
    }

    @Override
    public int getVersion() {
        return 14;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        // records can be inside of xml and csv files
        return file -> file.getFileType() == XmlFileType.INSTANCE || Objects.equals(file.getExtension(), "csv");
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public boolean indexDirectories() {
        return true;
    }

    private static class OdooRecordFileIndexer implements DataIndexer<String, OdooRecord, FileContent> {
        @Override
        public @NotNull Map<String, OdooRecord> map(@NotNull FileContent inputData) {
            if (inputData.getFileType().isBinary()) {
                // TODO we should make sure that XML and CSV are treated as text
                return Collections.emptyMap();
            }
            if (inputData.getFileType() == XmlFileType.INSTANCE) {
                PsiFile file = inputData.getPsiFile();
                return getRecordsFromFile(file, inputData.getFile().getPath());
            } else if (Objects.equals(inputData.getFile().getExtension(), "csv")) {
                PsiFile file = inputData.getPsiFile();
                return getRecordsFromCsvFile(file, inputData.getFile().getPath());
            }
            return Collections.emptyMap();
        }
    }
}
