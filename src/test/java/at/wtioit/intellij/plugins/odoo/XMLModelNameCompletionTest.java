package at.wtioit.intellij.plugins.odoo;

public class XMLModelNameCompletionTest extends BaseOdooPluginTest {

    public void testModelNameInModelAttribute() {
        doTest();
    }

    public void testModelNameInModelField() {
        doTest();
    }

    public void testModelNameInModelIdFieldNoAutoComplete() {
        doTest();
    }

    public void testModelNameInModelIdFieldRefNoAutoComplete() {
        doTest();
    }

    private void doTest() {
        myFixture.configureByFile("completion/xmlModelName/" + getTestName(true) + ".xml");
        myFixture.completeBasic();
        myFixture.checkResultByFile("completion/xmlModelName/" + getTestName(true) + ".after.xml");
    }

}
