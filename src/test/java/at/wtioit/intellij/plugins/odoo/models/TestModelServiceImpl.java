package at.wtioit.intellij.plugins.odoo.models;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.openapi.components.ServiceManager;

import java.util.stream.Collectors;

public class TestModelServiceImpl extends BaseOdooPluginTest {

    public void testFindingModels() {
        OdooModelService modelService = ServiceManager.getService(getProject(), OdooModelService.class);

        assertNull("Expected to get null for a not existing model", modelService.getModel("notExisting"));
        assertNotNull("Expected to get a model for an existing model", modelService.getModel("existing"));
        assertEquals("existing", modelService.getModel("existing").getName());
    }

    public void testModelInheritance() {
        OdooModelService modelService = ServiceManager.getService(getProject(), OdooModelService.class);
        OdooModel inherited = modelService.getModel("inherited");

        assertNotNull(inherited.getBaseModule().getDirectory());
        assertEquals("Base Model should be addon3", "PsiDirectory:/src/odoo/addons/addon3", inherited.getBaseModule().getDirectory().toString());
        assertContainsElements(inherited.getModules().stream().map(OdooModule::getName).collect(Collectors.toList()), "addon3", "addon1");
    }

}
