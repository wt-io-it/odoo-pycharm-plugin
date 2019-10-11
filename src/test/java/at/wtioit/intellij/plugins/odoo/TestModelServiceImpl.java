package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import com.intellij.openapi.components.ServiceManager;

public class TestModelServiceImpl extends BaseOdooPluginTest {

    public void testFindingModels() {
        OdooModelService modelService = ServiceManager.getService(getProject(), OdooModelService.class);

        assertNull("Expected to get null for a not existing model", modelService.getModel("notExisting"));
        assertNotNull("Expected to get a model for an existing model", modelService.getModel("existing"));
        assertEquals("existing", modelService.getModel("existing").getName());
    }

}
