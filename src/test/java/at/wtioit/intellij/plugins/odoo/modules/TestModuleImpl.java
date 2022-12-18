package at.wtioit.intellij.plugins.odoo.modules;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiDirectory;

import java.io.File;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotEquals;

public class TestModuleImpl extends BaseOdooPluginTest {

    public void testFindingModules() {
        OdooModuleService moduleService = getProject().getService(OdooModuleService.class);

        assertNull("Expected to get null for a not existing model", moduleService.getModule("notExisting"));
        OdooModule addon1 = moduleService.getModule("addon1");
        assertNotNull("Expected to get a model for an existing model", addon1);
        assertEquals("addon1", addon1.getName());
    }

    public void testFindingOcaModules() {
        OdooModuleService moduleService = getProject().getService(OdooModuleService.class);
        OdooModule ocaAddon = moduleService.getModule("oca_addon");
        WithinProject.run(getProject(), () -> {
            assertFalse(((PsiDirectory) ocaAddon.getDirectory()).getVirtualFile().getPath().contains(File.separator + "setup" + File.separator));
        });
    }

    public void testDependencies() {
        OdooModuleService moduleService = getProject().getService(OdooModuleService.class);
        OdooModule addon1 = moduleService.getModule("addon1");
        WithinProject.run(getProject(), () -> {
            assertContainsElements(addon1.getDependencies().stream().map(OdooModule::getName).collect(Collectors.toList()), "addon2");
        });
    }

    public void testAddonWithNoDependencies() {
        OdooModuleService moduleService = getProject().getService(OdooModuleService.class);
        OdooModule noDependencies = moduleService.getModule("no_dependencies");
        WithinProject.run(getProject(), () -> {
            assertEmpty(noDependencies.getDependencies());
        });
    }

    public void testAddonPathDisplay() {
        OdooModuleService moduleService = getProject().getService(OdooModuleService.class);
        OdooModule addon1 = moduleService.getModule("addon1");
        assertEquals("/src/odoo/addons/addon1", addon1.getRelativeLocationString());

        // TODO add test for resolving files inside of project (propably a heavy test)
    }

    public void testDependsOn() {
        OdooModuleService moduleService = getProject().getService(OdooModuleService.class);
        OdooModule addon1 = moduleService.getModule("addon1");

        // TODO remove WithinProject HACK
        try {
            WithinProject.INSTANCE.set(getProject());
            // test a direct dependency
            assertTrue(addon1.dependsOn(moduleService.getModule("addon2")));

            // test a non dependency
            assertFalse(addon1.dependsOn(moduleService.getModule("no_dependencies")));

            // test a transitive dependency
            assertTrue(addon1.dependsOn(moduleService.getModule("addon3")));
        } finally {
            WithinProject.INSTANCE.remove();
        }
    }

    public void testDependsOnRecursive() {
        OdooModuleService moduleService = getProject().getService(OdooModuleService.class);
        OdooModule addon1 = moduleService.getModule("recursion");

        // TODO remove WithinProject HACK
        try {
            WithinProject.INSTANCE.set(getProject());
            // test a direct dependency
            assertTrue(addon1.dependsOn(moduleService.getModule("recursion2")));

            // test a non dependency
            assertFalse(addon1.dependsOn(moduleService.getModule("no_dependencies")));

            // test a transitive dependency
            assertTrue(addon1.dependsOn(moduleService.getModule("addon1")));
        } finally {
            WithinProject.INSTANCE.remove();
        }
    }

    public void testOdooDirectory() {
        OdooModuleService moduleService = getProject().getService(OdooModuleService.class);
        assertEquals("odoo", moduleService.getOdooDirectory().getName());

        // TODO add test for other odoo directories
    }

    public void testFindingLaterAddedModule() {
        OdooModuleService moduleService = getProject().getService(OdooModuleService.class);
        OdooModule addon1 = moduleService.getModule("addon1");
        OdooModule addon1ViaFind = moduleService.findModule("addon1");

        assertSame(addon1, addon1ViaFind);

        assertNull(moduleService.findModule("not_yet_existing_addon"));

        myFixture.addFileToProject("later/not_yet_existing_addon/__manifest__.py", "{}");
        assertNotNull(moduleService.findModule("not_yet_existing_addon"));
    }

    public void testEquality() {
        OdooModuleService moduleService = getProject().getService(OdooModuleService.class);
        OdooModule addon1 = moduleService.getModule("addon1");
        OdooModule addon1Again = moduleService.getModule("addon1");
        OdooModule addon2 = moduleService.getModule("addon2");

        OdooModule addon2ViaDependency;
        // TODO remove WithinProject HACK
        try {
            WithinProject.INSTANCE.set(getProject());
            addon2ViaDependency = moduleService.getModule("addon1").getDependencies().stream().filter(dependency -> "addon2".equals(dependency.getName())).findFirst().orElseThrow(AssertionError::new);
        } finally {
            WithinProject.INSTANCE.remove();
        }

        assertSame(addon1, addon1Again);

        assertEquals(addon2, addon2ViaDependency);
        assertNotEquals(addon1, addon2);
        assertNotEquals(addon1, "addon1");

        assertEquals(addon2.hashCode(), addon2ViaDependency.hashCode());
        assertNotEquals(addon2.hashCode(), addon1.hashCode());
    }

    public void testStrangeAddonDirs() {
        OdooModuleService moduleService = getProject().getService(OdooModuleService.class);
        OdooModule addonWithWeirdSubdirs = moduleService.getModule("addon_with_weird_subdirs");
        assertNotNull(addonWithWeirdSubdirs);
        WithinProject.run(getProject(), () -> {
            assertTrue(addonWithWeirdSubdirs.getModels().stream().anyMatch(model -> "addon_with_weird_subdirs.my_model".equals(model.getName())));
            assertTrue(addonWithWeirdSubdirs.getModels().stream().anyMatch(model -> "addon_with_weird_subdirs.my_test_only_model".equals(model.getName())));
        });
    }


    // TODO add test for /posbox/
    // TODO add test for /remote_sources/


}
