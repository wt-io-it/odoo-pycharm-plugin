package at.wtioit.intellij.plugins.odoo.records;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TestRecordServiceImpl extends BaseOdooPluginTest {

    public void testFindingRecords() {
        OdooRecordService recordService = getProject().getService(OdooRecordService.class);

        assertNull("Expected to get no record for a not existing record", recordService.getRecord("addon1.not_existing_record"));
        assertNotNull("Expected to get a record for an existing record", recordService.getRecord("addon1.record1"));
        assertEquals("record1", recordService.getRecord("addon1.record1").getId());
        assertEquals("addon1.record1", recordService.getRecord("addon1.record1").getXmlId());
        assertEquals("record7", recordService.getRecord("addon1.record7").getId());
        assertEquals("addon1.record7", recordService.getRecord("addon1.record7").getXmlId());
        assertEquals("openerp_record", recordService.getRecord("addon1.openerp_record").getId());
        assertEquals("addon1.openerp_record", recordService.getRecord("addon1.openerp_record").getXmlId());
    }

    public void testHasRecord() {
        OdooRecordService recordService = getProject().getService(OdooRecordService.class);
        assertTrue(recordService.hasRecord("addon1.record1"));
        assertFalse(recordService.hasRecord("addon1.not_existing_record"));
        assertTrue(recordService.hasRecord("addon1.record7"));
        assertTrue(recordService.hasRecord("addon1.openerp_record"));
    }

    public void testHashRecordNonUniqueNames() {
        // those records are defined by multiple modules without a leading module name
        OdooRecordService recordService = getProject().getService(OdooRecordService.class);
        assertTrue(recordService.hasRecord("addon1.my_not_unique_record_name"));
        assertTrue(recordService.hasRecord("addon1_extension.my_not_unique_record_name"));
        assertFalse(recordService.hasRecord("addon2.my_not_unique_record_name"));
    }

    public void testRecordServiceConsistency() {
        OdooRecordService recordService = getProject().getService(OdooRecordService.class);
        String[] xmlIds = recordService.getXmlIds();
        assertContainsElements(Arrays.asList(xmlIds), "addon1.record1", "addon1.record2", ":UNDETECTED_XML_ID:.record7");
        for (String xmlId : xmlIds) {
            OdooRecord record = recordService.getRecord(xmlId);
            String detectedAddonName = xmlId.replaceAll("\\..*$", "");
            String idWithoutAddonName = record.getId().replace(detectedAddonName + ".", "");
            String idWithoutAddonNameFromXmlId = xmlId.replace(":UNDETECTED_XML_ID:.", "").replace(detectedAddonName + ".", "");
            assertEquals(idWithoutAddonNameFromXmlId, idWithoutAddonName);
            assertTrue(recordService.hasRecord(xmlId));
        }
    }

    public void testFindingRecordWithMultipleDefinitions() {
        OdooRecordService recordService = getProject().getService(OdooRecordService.class);
        for (Pair<String, String> recordExpectedModulePair : Arrays.asList(
                Pair.create("addon1.record2", "addon1"),
                Pair.create("addon1.assets_addon1", "addon2"))) {
            WithinProject.run(getProject(), () -> {
                String recordXmlId = recordExpectedModulePair.first;
                OdooRecord record = recordService.getRecord(recordXmlId);
                assertNotNull("Expected to get record for record existing in multiple files", record);

                OdooModuleService moduleService = getProject().getService(OdooModuleService.class);
                OdooModule module = moduleService.getModule(VirtualFileManager.getInstance().findFileByUrl("temp://" + record.getPath()));
                assertNotNull("Expected record to be in file that is resolvable to a module", module);
                String expectedModule = recordExpectedModulePair.second;
                assertEquals("Expected xmlId " + recordXmlId + " to resolve to record in addon1", expectedModule, module.getName());
            });
        }
    }

    public void testRecordXmlIds() {
        // test.record100 is read from this recordNameInRefAttribute.xml so we make sure it belongs to the project
        myFixture.configureByFile("goto/recordNameInRefAttribute.xml");
        OdooRecordService recordService = getProject().getService(OdooRecordService.class);
        assertContainsElements(Arrays.stream(recordService.getXmlIds()).collect(Collectors.toList()), "addon1.record4", "test.record100");
    }

    public void testGetRecordForElement() {
        PsiFile psiFile = myFixture.addFileToProject("odoo/addons/addon1/data/new_record.xml", "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<odoo>\n" +
                "    <data>\n" +
                "        <record id=\"new_record\" model=\"existing\" />\n" +
                "    </data>\n" +
                "</odoo>\n");

        OdooRecordService recordService = getProject().getService(OdooRecordService.class);
        OdooRecord record = recordService.getRecord("addon1.new_record");
        assertNotNull(record);
        assertEquals("new_record", record.getId());
    }

}
