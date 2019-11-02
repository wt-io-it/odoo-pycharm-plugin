package at.wtioit.intellij.plugins.odoo;

public class PythonModelNameCompletionTest extends BaseOdooPluginTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        forceRescan();
    }

    public void testModelNameInName() {
        doTest();
    }

    public void testModelNameInInherit() {
        doTest();
    }

    private void doTest() {
        myFixture.configureByFile("completion/pythonModelName/" + getTestName(true) + ".py");
        myFixture.completeBasic();
        myFixture.checkResultByFile("completion/pythonModelName/" + getTestName(true) + ".after.py");
    }


}
