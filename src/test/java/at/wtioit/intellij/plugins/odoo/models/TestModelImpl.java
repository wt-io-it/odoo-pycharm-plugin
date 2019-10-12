package at.wtioit.intellij.plugins.odoo.models;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import com.intellij.openapi.components.ServiceManager;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TestModelImpl extends BaseOdooPluginTest {

    public void testFindingModels() {
        OdooModelService modelService = ServiceManager.getService(getProject(), OdooModelService.class);

        assertContainsElements(StreamSupport.stream(modelService.getModelNames().spliterator(), false).collect(Collectors.toList()),
                "model_a",
                "model_b",
                "model_c",
                "model_d",
                "model_e");
    }

}
