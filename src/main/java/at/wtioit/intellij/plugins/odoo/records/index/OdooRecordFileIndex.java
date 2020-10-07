package at.wtioit.intellij.plugins.odoo.records.index;

import at.wtioit.intellij.plugins.odoo.AbstractDataExternalizer;
import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.*;
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
import java.util.*;

public class OdooRecordFileIndex extends FileBasedIndexExtension<String, OdooRecord> {

    private static final List ODOO_RECORD_TYPES = Arrays.asList("record", "template", "menuitem", "act_window", "report");

    private static final String NULL_XML_ID_KEY = ":UNDETECTED_XML_ID:";

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
        return new AbstractDataExternalizer<OdooRecord>() {
            @Override
            public void save(@NotNull DataOutput out, OdooRecord record) throws IOException {
                saveString(record.getXmlId(), out);
                saveString(record.getPath(), out);
                saveString(record.getModelName(), out);
                if (record instanceof OdooRecordImpl) {
                    saveString(((OdooRecordImpl) record).getId(), out);
                } else {
                    saveString(null, out);
                }
            }

            @Override
            public OdooRecord read(@NotNull DataInput in) throws IOException {
                String xmlId = readString(in);
                String path = readString(in);
                String modelName = readString(in);
                String id = readString(in);
                return new OdooDeserializedRecordImpl(id, xmlId, path, modelName);
            }
        };
    }

    @Override
    public int getVersion() {
        return 6;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        // an odoo module has __manifest__.py as a definition file
        return file -> file.getFileType() == XmlFileType.INSTANCE || file.getName().equals(".csv");
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
            HashMap<String, OdooRecord> records = new HashMap<>();
            if (inputData.getFileType() == XmlFileType.INSTANCE) {
                PsiFile file = inputData.getPsiFile();
                PsiElementsUtil.walkTree(file, (element) -> {
                    if (element instanceof XmlTag) {
                        XmlTag tag = (XmlTag) element;
                        if (tag.getNamespace().contains("http://relaxng.com/ns/")) {
                            // skip investigating relaxng schemas for odoo models
                            return true;
                        } else if ("odoo".equals(tag.getName())) {
                            records.putAll(getRecordsFromOdooTag(tag, inputData.getFile().getPath()));
                            return true;
                        }
                        // investigate children
                        return false;
                    } else if (element instanceof XmlDocument) {
                        // investigate children
                        return false;
                    }
                    // skip investigating children
                    return true;
                }, XmlElement.class, 3);
            }
            return records;
        }

        private Map<String, OdooRecord> getRecordsFromOdooTag(XmlTag odooTag, @NotNull String path) {
            HashMap<String, OdooRecord> records = new HashMap<>();
            PsiElementsUtil.walkTree(odooTag, (tag)-> {
                String name = tag.getName();
                // data needs further investigation (can hold records / templates)
                if ("data".equals(name)) return false;
                // function needs no further investigation (cannot hold records / templates)
                if ("function".equals(name)) return true;
                if (ODOO_RECORD_TYPES.contains(name)) {
                    OdooRecord record = OdooRecordImpl.getFromXml(tag, path);
                    if (record.getXmlId() == null) {
                        records.put(NULL_XML_ID_KEY, record);
                    } else {
                        records.put(record.getXmlId(), record);
                    }
                    return true;
                }
                return false;
            }, XmlTag.class, 2);
            return records;
        }
    }
}
