package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.components.ServiceManager;

import java.util.stream.Collectors;

import static org.junit.Assert.assertNotEquals;

public class TestModuleImpl extends BaseOdooPluginTest {

    public void testFindingModules() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);

        assertNull("Expected to get null for a not existing model", moduleService.getModule("notExisting"));
        OdooModule addon1 = moduleService.getModule("addon1");
        assertNotNull("Expected to get a model for an existing model", addon1);
        assertEquals("addon1", addon1.getName());
    }

    public void testDependencies() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);
        OdooModule addon1 = moduleService.getModule("addon1");
        assertContainsElements(addon1.getDependencies().stream().map(OdooModule::getName).collect(Collectors.toList()), "addon2");
    }

    public void testAddonWithNoDependencies() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);
        OdooModule noDependencies = moduleService.getModule("no_dependencies");
        assertEmpty(noDependencies.getDependencies());
    }

    public void testAddonPathDisplay() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);
        OdooModule addon1 = moduleService.getModule("addon1");
        assertEquals("/src/odoo/addons/addon1", addon1.getRelativeLocationString());

        // TODO add test for resolving files inside of project (propably a heavy test)
    }

    public void testDependsOn() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);
        OdooModule addon1 = moduleService.getModule("addon1");

        // test a direct dependency
        assertTrue(addon1.dependsOn(moduleService.getModule("addon2")));

        // test a non dependency
        assertFalse(addon1.dependsOn(moduleService.getModule("no_dependencies")));

        // test a transitive dependency
        assertTrue(addon1.dependsOn(moduleService.getModule("addon3")));
    }

    public void testOdooDirectory() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);
        assertEquals("odoo", moduleService.getOdooDirectory().getName());

        // TODO add test for other odoo directories
    }

    public void testFindingModule() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);
        OdooModule addon1 = moduleService.getModule("addon1");
        OdooModule addon1ViaFind = moduleService.findModule("addon1");

        assertSame(addon1, addon1ViaFind);

        assertNull(moduleService.findModule("not_yet_existing_addon"));

        myFixture.addFileToProject("later/not_yet_existing_addon/__manifest__.py", "{}");
        assertNotNull(moduleService.findModule("not_yet_existing_addon"));
    }

    public void testEquality() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);
        OdooModule addon1 = moduleService.getModule("addon1");
        OdooModule addon1Again = moduleService.getModule("addon1");
        OdooModule addon2 = moduleService.getModule("addon2");
        OdooModule addon2ViaDependency = moduleService.getModule("addon1").getDependencies().stream().filter(dependency -> "addon2".equals(dependency.getName())).findFirst().orElseThrow(AssertionError::new);

        assertSame(addon1, addon1Again);

        assertEquals(addon2, addon2ViaDependency);
        assertNotEquals(addon1, addon2);
        assertNotEquals(addon1, "addon1");

        assertEquals(addon2.hashCode(), addon2ViaDependency.hashCode());
        assertNotEquals(addon2.hashCode(), addon1.hashCode());
    }



}
