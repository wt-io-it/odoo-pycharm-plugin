package at.wtioit.intellij.plugins.odoo.records.index;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.records.AbstractOdooRecord;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.util.HashMap;
import java.util.Objects;

import static at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil.getRecordsFromFile;

public class OdooDeserializedRecordImpl extends AbstractOdooRecord {

    public OdooDeserializedRecordImpl(String id, String xmlId, String path, String modelName) {
        super(id, xmlId, modelName, path);
    }

    @Override
    public PsiElement getDefiningElement() {
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl("file:///" + getPath());
        if (virtualFile != null) {
            PsiFile file = PsiManager.getInstance(WithinProject.INSTANCE.get()).findFile(virtualFile);
            HashMap<String, OdooRecord> recordsFromFile = getRecordsFromFile(file, (record) -> Objects.equals(getXmlId(), record.getXmlId()), 1);
            if (recordsFromFile.size() == 1) {
                OdooRecord record = recordsFromFile.values().iterator().next();
                return record.getDefiningElement();
            }
        }
        return null;
    }
}
