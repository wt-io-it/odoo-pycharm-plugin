package at.wtioit.intellij.plugins.odoo.models.index;

import at.wtioit.intellij.plugins.odoo.AbstractDataExternalizer;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.OdooModelUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OdooModelFileIndex extends FileBasedIndexExtension<String, OdooModelDefinition> {

    @NonNls public static final ID<String, OdooModelDefinition> NAME = ID.create("OdooModelFileIndex");

    OdooModelFileIndexer indexer = new OdooModelFileIndexer();

    @Override
    public @NotNull ID<String, OdooModelDefinition> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, OdooModelDefinition, FileContent> getIndexer() {
        return indexer;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<OdooModelDefinition> getValueExternalizer() {
        return new AbstractDataExternalizer<OdooModelDefinition>() {
            @Override
            public void save(@NotNull DataOutput out, OdooModelDefinition model) throws IOException {
                saveString(model.getProject(), out);
                saveString(model.getFileName(), out);
                saveString(model.getClassName(), out);
                saveString(model.getName(), out);
            }

            @Override
            public OdooModelDefinition read(@NotNull DataInput in) throws IOException {
                String projectPresentableUrl = readString(in);
                String fileName = readString(in);
                String className = readString(in);
                String modelName = readString(in);
                for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                    if (projectPresentableUrl.equals(project.getPresentableUrl())) {
                        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
                        for (PsiFile file : FilenameIndex.getFilesByName(project, fileName, scope)) {
                            for (PsiElement element : file.getChildren()) {
                                if (element instanceof PyClass) {
                                    PyClass pyClass = (PyClass) element;
                                    if (Objects.equals(pyClass.getName(), className) && OdooModelUtil.detectName(pyClass).equals(modelName)) {
                                        return new OdooModelDefinition(pyClass);
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }


        };
    }

    @Override
    public int getVersion() {
        return 6;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull VirtualFile file) {
                // TODO maybe check that we are inside of a possible module
                return file.getFileType() == PythonFileType.INSTANCE;
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    private static class OdooModelFileIndexer implements DataIndexer<String, OdooModelDefinition, FileContent> {

        private final Logger logger = Logger.getInstance(OdooModelFileIndexer.class);

        @Override
        public @NotNull Map<String, OdooModelDefinition> map(@NotNull FileContent inputData) {
            HashMap<String, OdooModelDefinition> models = new HashMap<>();
            @NotNull PsiFile file = inputData.getPsiFile();
            for (PsiElement pyline : file.getChildren()) {
                if (OdooModelService.isOdooModelDefinition(pyline)) {
                    logger.debug("Found " + pyline + " in " + file.getName());
                    OdooModelDefinition model = new OdooModelDefinition((PyClass) pyline);
                    models.put(model.getName(), model);
                }
            }
            return models;
        }
    }
}
