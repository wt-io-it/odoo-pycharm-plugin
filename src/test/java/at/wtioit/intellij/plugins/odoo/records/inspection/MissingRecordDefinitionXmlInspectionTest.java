package at.wtioit.intellij.plugins.odoo.records.inspection;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.jetbrains.python.inspections.PyStatementEffectInspection;

public class MissingRecordDefinitionXmlInspectionTest extends BaseOdooPluginTest {

    public void testNoExtraProblemsForEmptyDir() {
        // we test that the exception suppression for __manifest__.py files works
        doTest();
    }

    public void testNoExtraProblemsForEmptyTemplates() {
        // we test that the exception suppression for __manifest__.py files works
        doTest();
    }

    public void testNoExtraProblemsForEmptyXmls() {
        // we test that the exception suppression for __manifest__.py files works
        doTest();
    }

    public void testNoExtraProblemsForExistingTemplateReferences() {
        // we test that the exception suppression for __manifest__.py files works
        doTest();
    }

    public void testMissingRecordForNotExistingTemplateReferences() {
        // we test that the exception suppression for __manifest__.py files works
        doTest();
    }

    public void testNoExtraProblemsForExistingModelModuleReferences() {
        // we test that we can resolve module_ and model_ pseudo xml ids
        doTest();
    }

    public void testMissingRecordForNotExistingModuleModelReferences() {
        // we test that we can resolve module_ and model_ pseudo xml ids
        doTest();
    }

    private void doTest() {
        LocalInspectionToolWrapper toolWrapper = new LocalInspectionToolWrapper(new MissingRecordDefinitionXmlInspection());
        myFixture.testInspection("inspection/records/" + getTestName(true), toolWrapper);
    }
}
