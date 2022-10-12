package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.records.OdooRecordService;
import com.intellij.openapi.components.ServiceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IndexingKeysTest extends BaseOdooPluginTest {
    public void testModuleIndexKeys() {
        OdooModuleService service = ServiceManager.getService(myFixture.getProject(), OdooModuleService.class);
        assertNotNull(service);
        ArrayList<OdooModule> modules = new ArrayList<>();
        service.getModules().forEach(modules::add);
        List<String> moduleNames = modules.stream().map(OdooModule::getName).sorted().collect(Collectors.toList());
        assertContainsElements(moduleNames, Arrays.asList(
                "addon1",
                "addon2",
                "addon3",
                "addon_with_weird_subdirs",
                "autocomplete",
                "invalid_manifest",
                "model_names",
                "my_other_addon",
                "no_dependencies",
                "oca_addon"));
    }

    public void testModelIndexKeys() {
        OdooModelService service = ServiceManager.getService(myFixture.getProject(), OdooModelService.class);
        assertNotNull(service);
        List<String> modelNames = new ArrayList<>();
        service.getModelNames().forEach(modelNames::add);
        modelNames = modelNames.stream().sorted().collect(Collectors.toList());
        assertContainsElements(modelNames, Arrays.asList(
                "addon_with_weird_subdirs.my_model",
                "addon_with_weird_subdirs.my_test_only_model",
                "existing",
                "inherited",
                "mixed_wildcard.default_:ANYTHING:",
                "mixed_wildcard.default_explicit",
                "mixed_wildcard.format_:ANYTHING:",
                "mixed_wildcard.format_explicit",
                "model_a",
                "model_b",
                "model_c",
                "model_d",
                "model_e",
                "outer_model",
                "single_wildcard.:ANYTHING:",
                "well_described"));
    }


    public void testRecordIndexKeys() {
        OdooRecordService service = ServiceManager.getService(myFixture.getProject(), OdooRecordService.class);
        assertNotNull(service);
        List<String> recordNames = Arrays.asList(service.getXmlIds());
        recordNames = recordNames.stream().sorted().collect(Collectors.toList());
        assertContainsElements(recordNames, Arrays.asList(
                ":UNDETECTED_XML_ID:.record10",
                ":UNDETECTED_XML_ID:.record11",
                ":UNDETECTED_XML_ID:.record14",
                ":UNDETECTED_XML_ID:.record15",
                ":UNDETECTED_XML_ID:.record16",
                ":UNDETECTED_XML_ID:.record5",
                ":UNDETECTED_XML_ID:.record6",
                ":UNDETECTED_XML_ID:.record7",
                ":UNDETECTED_XML_ID:.record8",
                ":UNDETECTED_XML_ID:.record9",
                "addon1.Board1",
                "addon1.existing_kanban_view",
                "addon1.inherited",
                "addon1.my_not_unique_record_name",
                "addon1.open_existing_dashboard_kanban",
                "addon1.openerp_record",
                "addon1.record1",
                "addon1.record2",
                "addon1.record3",
                "addon1.record4",
                "addon1_extension.my_not_unique_record_name",
                "Board1.action"));
    }
}
