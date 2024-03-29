package at.wtioit.intellij.plugins.odoo.records.index;

import at.wtioit.intellij.plugins.odoo.AbstractDataExternalizer;
import at.wtioit.intellij.plugins.odoo.OdooRecordPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexEntry;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexError;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexExtension;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.PythonFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

import static at.wtioit.intellij.plugins.odoo.OdooRecordPsiElementMatcherUtil.getRecordsFromCsvFile;

public class OdooRecordFileIndex extends OdooIndexExtension<OdooRecord> {

    @NonNls public static final ID<String, OdooRecord> NAME = ID.create("OdooRecordFileIndex");

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
        // TODO only hand out one instance
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
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        // records can be inside of xml and csv files
        return file -> file.getFileType() == XmlFileType.INSTANCE // all xml files
                || (file.getFileType() == PythonFileType.INSTANCE && "__manifest__.py".equals(file.getName())) // __manifest__.py files for assets
                || Objects.equals(file.getExtension(), "csv"); // csv files
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
        private static final List<String> CSV_FILETYPE_CLASS_NAMES = Arrays.asList(
                "com.intellij.openapi.fileTypes.PlainTextFileType", // 2022.1 for csv
                "com.intellij.database.csv.CsvFileType" // 2021.3 for csv
        );

        @Override
        public @NotNull Map<String, OdooRecord> map(@NotNull FileContent inputData) {
            FileType fileType = inputData.getFileType();
            if (fileType.isBinary()) {
                // TODO we should make sure that XML and CSV are treated as text
                return Collections.emptyMap();
            }
            if (fileType == XmlFileType.INSTANCE || fileType == PythonFileType.INSTANCE) {
                PsiFile file = inputData.getPsiFile();
                return OdooRecordPsiElementMatcherUtil.getRecordsFromFile(file, inputData.getFile().getPath());
            } else if (Objects.equals(inputData.getFile().getExtension(), "csv")) {
                // we use names of classes here, as the presence of the classes wildly differs between versions
                if (CSV_FILETYPE_CLASS_NAMES.contains(fileType.getClass().getName())) {
                    PsiFile file = inputData.getPsiFile();
                    return getRecordsFromCsvFile(file, inputData.getFile().getPath());
                } else {
                    // it seems starting with 2022.1 we cannot support indexing CSVs without an explicit file type
                    // TODO provide our own basic CSV filetype for indexing for those files
                    // * DetectedByContentFileType (new in 2022.1) (temp:///src/odoo/addons/addon1/data/existing.csv)
                    return Collections.emptyMap();
                }
            }
            return Collections.emptyMap();
        }
    }

    @Override
    public <E extends OdooIndexEntry> OdooRecord castValue(E entry) {
        if (entry instanceof OdooRecord) {
            return (OdooRecord) entry;
        }
        throw new OdooIndexError("expected entry to be of type OdooRecord");
    }
}
