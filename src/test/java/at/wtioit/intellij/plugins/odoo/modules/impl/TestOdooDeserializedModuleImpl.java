package at.wtioit.intellij.plugins.odoo.modules.impl;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.modules.index.OdooDeserializedModuleImpl;

import java.io.File;

public class TestOdooDeserializedModuleImpl extends BaseOdooPluginTest {

    public void testGetRelativePath() {
        String basePath = getProject().getBasePath();
        WithinProject.run(getProject(), () -> {
            assertEquals("module1.py", OdooDeserializedModuleImpl.getInstance("module1", basePath + File.separator + "module1.py").getRelativeLocationString());
            assertEquals(basePath, OdooDeserializedModuleImpl.getInstance("module1", basePath).getRelativeLocationString());
            assertEquals("not_in_the_base_path", OdooDeserializedModuleImpl.getInstance("module1", "not_in_the_base_path").getRelativeLocationString());

        });
    }
}
