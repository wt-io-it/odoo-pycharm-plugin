package at.wtioit.intellij.plugins.odoo.models.inspection;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;

public class MissingModelDefinitionInspectionTest extends BaseOdooPluginTest {

    public void testNoExtraProblemsForEmptyDir() {
        doTest();
    }

    public void testNoExtraProblemsForCorrectModelNames() {
        doTest();
    }

    public void testExtraProblemsForWrongModelNames() {
        doTest();
    }

    private void doTest() {
        LocalInspectionToolWrapper toolWrapper = new LocalInspectionToolWrapper(new MissingModelDefinitionInspection());
        myFixture.testInspection("inspection/" + getTestName(true), toolWrapper);
    }
}
