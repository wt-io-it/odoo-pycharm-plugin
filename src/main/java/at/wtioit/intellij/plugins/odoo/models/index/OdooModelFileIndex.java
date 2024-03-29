package at.wtioit.intellij.plugins.odoo.models.index;

import at.wtioit.intellij.plugins.odoo.AbstractDataExternalizer;
import at.wtioit.intellij.plugins.odoo.index.OdooDataIndexer;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexEntry;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexError;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexExtension;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
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
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil.getModelsFromFile;

public class OdooModelFileIndex extends OdooIndexExtension<OdooModelIE> {

    @NonNls public static final ID<String, OdooModelIE> NAME = ID.create("OdooModelFileIndex");

    OdooModelFileIndexer indexer = new OdooModelFileIndexer();

    @Override
    public @NotNull ID<String, OdooModelIE> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, OdooModelIE, FileContent> getIndexer() {
        return indexer;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<OdooModelIE> getValueExternalizer() {
        return new AbstractDataExternalizer<OdooModelIE>() {
            @Override
            public void save(@NotNull DataOutput out, OdooModelIE model) throws IOException {
                if (model != null) {
                    saveString(model.getProject(), out);
                    saveString(model.getFileName(), out);
                    saveString(model.getClassName(), out);
                    saveString(model.getName(), out);
                }
            }

            @Override
            public OdooModelIE read(@NotNull DataInput in) throws IOException {
                String projectPresentableUrl = readString(in);
                String fileName = readString(in);
                String className = readString(in);
                String modelName = readString(in);
                for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                    if (projectPresentableUrl.equals(project.getPresentableUrl())) {
                        return new OdooModelIE(fileName, className, modelName, project);
                    }
                }
                // this happens when we get index results from projects no longer open
                return new OdooModelIE(fileName, className, modelName, projectPresentableUrl);
            }


        };
    }

    @Override
    public int getVersion() {
        return 21;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> {
            // TODO maybe check that we are inside of a possible module
            return file.getFileType() == PythonFileType.INSTANCE
                    // OCA modules use symlinked setup directories that we should ignore
                    && !file.getPath().contains(File.separator + "setup" + File.separator)
                    // when using remote debugging (e.g. with docker) pycharm may have remote sources that duplicate our modules
                    && !file.getPath().contains(File.separator + "remote_sources" + File.separator);
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    private static class OdooModelFileIndexer extends OdooDataIndexer<OdooModelIE> {

        private final Logger logger = Logger.getInstance(OdooModelFileIndexer.class);

        @Override
        public @NotNull Map<String, OdooModelIE> mapWatched(@NotNull FileContent inputData) {
            return getModelsFromFile(inputData.getPsiFile());
        }
    }

    @Override
    public <E extends OdooIndexEntry> OdooModelIE castValue(E entry) {
        if (entry instanceof OdooModelIE) {
            return (OdooModelIE) entry;
        }
        throw new OdooIndexError("expected entry to be of type OdooModelDefinition");
    }
}
