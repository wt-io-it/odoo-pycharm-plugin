package at.wtioit.intellij.plugins.odoo.records.index;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.index.OdooModelIE;
import at.wtioit.intellij.plugins.odoo.records.AbstractOdooRecord;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

import static at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil.getModelsFromFile;
import static at.wtioit.intellij.plugins.odoo.OdooRecordPsiElementMatcherUtil.getRecordsFromFile;

public class OdooDeserializedRecordImpl extends AbstractOdooRecord {

    public OdooDeserializedRecordImpl(String id, String xmlId, String path, String modelName) {
        super(id, xmlId, modelName, path);
    }

    @Override
    public PsiElement getDefiningElement() {
        VirtualFile virtualFile = findVirtualFile();
        if (virtualFile != null) {
            PsiFile file = PsiManager.getInstance(WithinProject.INSTANCE.get()).findFile(virtualFile);
            if (file != null) {
                HashMap<String, OdooRecord> recordsFromFile = getRecordsFromFile(file, (record) -> {
                    return Objects.equals(getXmlId(), record.getXmlId());
                }, 1);
                if (recordsFromFile.size() == 1) {
                    OdooRecord record = recordsFromFile.values().iterator().next();
                    return record.getDefiningElement();
                } else if (getXmlId() == null && recordsFromFile.size() > 0) {
                    // find matching ids
                    for (OdooRecord record : recordsFromFile.values()) {
                        if (Objects.equals(record.getId(), getId())) {
                            return record.getDefiningElement();
                        }
                    }
                }
                if (getXmlId().startsWith("base.model_")) {
                    HashMap<String, OdooModelIE> modelsFromFile = getModelsFromFile(file, (model) -> {
                        return Objects.equals(getXmlId(), "base.model_" + model.getName().replace(".", "_"));
                    }, 1);
                    if (modelsFromFile.size() == 1) {
                        OdooModelIE modelDefinition = modelsFromFile.values().iterator().next();
                        OdooModel model = WithinProject.INSTANCE.get().getService(OdooModelService.class).getModel(modelDefinition.getName());
                        return model.getDefiningElement();
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public VirtualFile findVirtualFile() {
        VirtualFileManager fileManager = VirtualFileManager.getInstance();
        VirtualFile virtualFile = fileManager.findFileByUrl("file:///" + getPath());
        if (virtualFile == null) {
            // Mostly enable tests where files for the current tests are configured as temp files
            virtualFile = fileManager.findFileByUrl("temp:///" + getPath());
        }
        return virtualFile;
    }
}
