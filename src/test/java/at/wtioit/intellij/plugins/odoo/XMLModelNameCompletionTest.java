package at.wtioit.intellij.plugins.odoo;

public class XMLModelNameCompletionTest extends BaseOdooPluginTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        forceRescan();
    }

    public void testModelNameInModelAttribute() {
        doTest();
    }

    private void doTest() {
        myFixture.configureByFile("completion/xmlModelName/" + getTestName(true) + ".xml");
        myFixture.completeBasic();
        myFixture.checkResultByFile("completion/xmlModelName/" + getTestName(true) + ".after.xml");
    }


}
