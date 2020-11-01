package at.wtioit.intellij.plugins.odoo.records;

import com.intellij.psi.PsiFile;

public interface OdooRecordService {
    //TODO use iterable for memory consumption
    String[] getXmlIds();

    OdooRecord getRecord(String xmlId);

    boolean hasRecord(String xmlId);

    String ensureFullXmlId(PsiFile file, String refName);
}
