package at.wtioit.intellij.plugins.odoo.models;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TestModelServiceImpl extends BaseOdooPluginTest {

    public void testFindingModels() {
        OdooModelService modelService = getProject().getService(OdooModelService.class);

        assertNull("Expected to get null for a not existing model", modelService.getModel("notExisting"));
        assertNotNull("Expected to get a model for an existing model", modelService.getModel("existing"));
        assertEquals("existing", modelService.getModel("existing").getName());
    }

    public void testModelInheritance() {
        OdooModelService modelService = getProject().getService(OdooModelService.class);
        OdooModel inherited = modelService.getModel("inherited");
        WithinProject.run(getProject(), () -> {
            assertNotNull(inherited.getBaseModule().getDirectory());
            assertEquals("Base Model should be addon3", "PsiDirectory:/src/odoo/addons/addon3", inherited.getBaseModule().getDirectory().toString());
            assertContainsElements(inherited.getModules().stream().map(OdooModule::getName).collect(Collectors.toList()), "addon3", "addon1");
        });
    }

    public void testModelNames() {
        OdooModelService modelService = getProject().getService(OdooModelService.class);
        assertContainsElements(StreamSupport.stream(modelService.getModelNames().spliterator(), false).collect(Collectors.toList()), "inherited", "existing");
    }

    public void testGetModelForElement() {
        PsiFile psiFile = myFixture.addFileToProject("odoo/addons/addon1/models/model_for_element.py", "" +
                "from odoo import models\n" +
                "\n" +
                "class ModelForElement(models.Model):" +
                "   _name = 'model_for_element'");
        PsiElement pyClass = Arrays.stream(psiFile.getChildren()).filter(psiElement -> psiElement instanceof PyClass).findFirst().orElseThrow(AssertionError::new);

        OdooModelService modelService = getProject().getService(OdooModelService.class);
        OdooModel modelForElement = modelService.getModelForElement(pyClass);
        assertNotNull(modelForElement);
        assertEquals("model_for_element", modelForElement.getName());
    }

}
