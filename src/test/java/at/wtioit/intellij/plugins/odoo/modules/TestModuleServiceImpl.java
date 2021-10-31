package at.wtioit.intellij.plugins.odoo.modules;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiFile;

public class TestModuleServiceImpl extends BaseOdooPluginTest {

    public void testFindingModule() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);

        OdooModule module = moduleService.getModule("addon1");
        assertNotNull("Expected to get module for existing directory with manifest", module);
    }

    public void testFindingModuleWithVirtualFile() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);

        OdooModule module = moduleService.getModule("addon1");
        WithinProject.run(getProject(), () -> {
            PsiFile manifestFile = module.getManifestFile();
            OdooModule moduleFromFile = moduleService.getModule(manifestFile.getVirtualFile());
            assertNotNull("Expected to get module for existing directory with manifest", moduleFromFile);
            assertEquals("Expected to get the same (equal) module by file as by name", moduleFromFile, module);
        });

    }

}
