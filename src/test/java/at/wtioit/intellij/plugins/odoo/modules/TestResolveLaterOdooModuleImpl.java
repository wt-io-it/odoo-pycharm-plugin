package at.wtioit.intellij.plugins.odoo.modules;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.impl.ResolveLaterOdooModuleImpl;
import com.intellij.openapi.components.ServiceManager;
import org.apache.commons.lang.NotImplementedException;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotEquals;

public class TestResolveLaterOdooModuleImpl extends BaseOdooPluginTest {

    public void testFallbackValues() {
        ResolveLaterOdooModuleImpl resolveLater = new ResolveLaterOdooModuleImpl("resolve_me_later", getProject());

        assertEquals("resolve_me_later", resolveLater.getName());
        assertNull(resolveLater.getRelativeLocationString());
        assertNull(resolveLater.getIcon());
        assertNull(resolveLater.getDirectory());
        assertEmpty(resolveLater.getDependencies());
        assertEmpty(resolveLater.getModels());
        assertFalse(resolveLater.dependsOn(new ResolveLaterOdooModuleImpl("not_resolving", getProject())));
    }


    public void testResolving() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);
        ResolveLaterOdooModuleImpl resolveLater = new ResolveLaterOdooModuleImpl("resolve_me_later_too", getProject());

        myFixture.addFileToProject("repo2/resolve_me_later_too/__manifest__.py", "" +
                "{\n" +
                "   'depends': ['addon1'],\n" +
                "}\n");
        myFixture.addFileToProject("repo2/resolve_me_later_too/models/my_model.py", "" +
                "from odoo import models\n" +
                "\n" +
                "class MyModel(models.Model):\n" +
                "   _name = 'my.model'\n");
        forceRescan();

        assertEquals("resolve_me_later_too", resolveLater.getName());
        assertEquals("/src/repo2/resolve_me_later_too", resolveLater.getRelativeLocationString());
        assertSame(moduleService.getModule("resolve_me_later_too").getIcon(), resolveLater.getIcon());
        assertSame(moduleService.getModule("resolve_me_later_too").getDirectory(), resolveLater.getDirectory());
        assertContainsElements(resolveLater.getDependencies().stream().map(OdooModule::getName).collect(Collectors.toList()), "addon1");
        assertContainsElements(resolveLater.getModels().stream().map(OdooModel::getName).collect(Collectors.toList()), "my.model");

        assertFalse(resolveLater.dependsOn(new ResolveLaterOdooModuleImpl("not_resolving", getProject())));

        assertTrue(resolveLater.dependsOn(moduleService.getModule("addon1")));
    }

}
