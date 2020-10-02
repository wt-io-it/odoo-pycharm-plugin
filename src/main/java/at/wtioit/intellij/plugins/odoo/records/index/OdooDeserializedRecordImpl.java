package at.wtioit.intellij.plugins.odoo.records.index;

import at.wtioit.intellij.plugins.odoo.records.AbstractOdooRecord;

public class OdooDeserializedRecordImpl extends AbstractOdooRecord {

    public OdooDeserializedRecordImpl(String id, String xmlId, String path, String modelName) {
        super(id, xmlId, modelName, path);
    }

}
