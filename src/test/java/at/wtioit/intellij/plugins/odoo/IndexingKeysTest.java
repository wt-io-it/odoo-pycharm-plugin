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
                "mixed_wildcard.:ANYTHING:",
                "mixed_wildcard.explicit",
                "model_a",
                "model_b",
                "model_c",
                "model_d",
                "model_e",
                "outer_model",
                "well_described",
                "wildcard.:ANYTHING:"));
    }


    public void testRecordIndexKeys() {
        OdooRecordService service = ServiceManager.getService(myFixture.getProject(), OdooRecordService.class);
        assertNotNull(service);
        List<String> recordNames = Arrays.asList(service.getXmlIds());
        recordNames = recordNames.stream().sorted().collect(Collectors.toList());
        assertContainsElements(recordNames, Arrays.asList(
                ":UNDETECTED_XML_ID:.record1",
                ":UNDETECTED_XML_ID:.record3",
                ":UNDETECTED_XML_ID:.record5",
                ":UNDETECTED_XML_ID:.record6",
                ":UNDETECTED_XML_ID:.record7",
                ":UNDETECTED_XML_ID:.record8",
                ":UNDETECTED_XML_ID:.record9",
                ":UNDETECTED_XML_ID:.record10",
                ":UNDETECTED_XML_ID:.record11",
                ":UNDETECTED_XML_ID:.record14",
                "addon1.record2",
                "addon1.record4"));
    }
}