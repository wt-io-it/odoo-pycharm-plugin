package at.wtioit.intellij.plugins.odoo.records.index;

import junit.framework.TestCase;

public class TestOdooRecordImpl extends TestCase {
    public void testGuessingXmlIdsFromPath() {
        assertNull(OdooRecordImpl.guessFromPath("user", "/home/user/Documents/test.xml"));
        assertEquals("addon1.record", OdooRecordImpl.guessFromPath("record", "/odoo/addons/addon1/data/records.xml"));
        assertEquals("addon2.view", OdooRecordImpl.guessFromPath("view", "/odoo/addons/addon2/views/views.xml"));
        assertEquals("addon3.template", OdooRecordImpl.guessFromPath("template", "/odoo/addons/addon3/static/src/xml/templates.xml"));
    }
}
