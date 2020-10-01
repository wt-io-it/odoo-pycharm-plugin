package at.wtioit.intellij.plugins.odoo.modules.inspection;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.models.inspection.MissingModelDefinitionInspection;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.jetbrains.python.inspections.PyStatementEffectInspection;

public class ManifestInspectionTest extends BaseOdooPluginTest {

    public void testNoExtraProblemsForEmptyDir() {
        // we test that the exception suppression for __manifest__.py files works
        doTest();
    }

    public void testOtherProblemsStillRecognized() {
        // we test that the exception suppression for __manifest__.py files works
        doTest();
    }

    private void doTest() {
        LocalInspectionToolWrapper toolWrapper = new LocalInspectionToolWrapper(new PyStatementEffectInspection());
        myFixture.testInspection("inspection/modules/" + getTestName(true), toolWrapper);
    }
}
