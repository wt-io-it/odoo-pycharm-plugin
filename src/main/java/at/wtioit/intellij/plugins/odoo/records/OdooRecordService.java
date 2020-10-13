package at.wtioit.intellij.plugins.odoo.records;

public interface OdooRecordService {
    String[] getXmlIds();

    OdooRecord getRecord(String xmlId);
}
