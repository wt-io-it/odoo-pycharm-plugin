package at.wtioit.intellij.plugins.odoo.records;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TestRecordServiceImpl extends BaseOdooPluginTest {

    public void testFindingRecords() {
        OdooRecordService recordService = ServiceManager.getService(getProject(), OdooRecordService.class);

        assertNull("Expected to get a record for an existing record", recordService.getRecord("addon1.not_existing_record"));
        assertNotNull("Expected to get a record for an existing record", recordService.getRecord("addon1.record1"));
        // TODO this should work but currently the xmlId is empty if we cannot find it right away (during first index pass)
        //assertEquals("addon1.record1", recordService.getRecord("addon1.record1").getXmlId());
        assertEquals("record1", recordService.getRecord("addon1.record1").getId());
    }

    public void testFindingRecordWithMultipleDefinitions() {
        OdooRecordService recordService = ServiceManager.getService(getProject(), OdooRecordService.class);
        WithinProject.run(getProject(), () -> {
            OdooRecord record = recordService.getRecord("addon1.record2");
            assertNotNull("Expected to get record for record existing in multiple files", record);

            OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);
            OdooModule module = moduleService.getModule(VirtualFileManager.getInstance().findFileByUrl("temp://" + record.getPath()));
            assertNotNull("Expected record to be in file that is resolvable to a module", module);
            assertEquals("Expected xmlId adddon1.record2 to resolve to record in addon1", "addon1", module.getName());
        });
    }

    public void testRecordXmlIds() {
        // test.record100 is read from this recordNameInRefAttribute.xml so we make sure it belongs to the project
        myFixture.configureByFile("goto/recordNameInRefAttribute.xml");
        OdooRecordService recordService = ServiceManager.getService(getProject(), OdooRecordService.class);
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

        OdooRecordService recordService = ServiceManager.getService(getProject(), OdooRecordService.class);
        OdooRecord record = recordService.getRecord("addon1.new_record");
        assertNotNull(record);
        assertEquals("new_record", record.getId());
    }

}
