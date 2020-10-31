package at.wtioit.intellij.plugins.odoo.records;

import com.intellij.psi.PsiFile;

public interface OdooRecordService {
    String[] getXmlIds();

    OdooRecord getRecord(String xmlId);

    boolean hasRecord(String xmlId);

    String ensureFullXmlId(PsiFile file, String refName);
}
